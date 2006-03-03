package dart.server;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;
import java.text.*;
import java.util.Properties;

// import marquee.xmlrpc.XmlRpcServer;
import org.apache.xmlrpc.XmlRpcServer;
import net.sourceforge.jaxor.JaxorContextImpl;
import net.sourceforge.jaxor.QueryParams;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.mortbay.http.DigestAuthenticator;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.handler.NotFoundHandler;
import org.mortbay.http.handler.ResourceHandler;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import dart.server.task.ScheduledTask;
import dart.server.task.Task;
import dart.server.task.*;
import dart.server.wrap.TaskQueueEntity;
import dart.server.wrap.TaskQueueFinderBase;
import dart.server.servlet.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.log4j.Logger;

public class Project extends Container {

  static final int DBMajorVersion = 1;
  static final int DBMinorVersion = 0;
  static final String DBVersionString = "" + DBMajorVersion + "." + DBMinorVersion;

  static Logger logger = Logger.getLogger ( Project.class );
  static Properties statsDefaults = new Properties();
  static {
    statsDefaults.put ( "Startups", "0" );
    statsDefaults.put ( "Submissions", "0" );
    statsDefaults.put ( "TestsProcessed", "0" );
    statsDefaults.put ( "BinaryResults", "0" );
  }

  TrackManager trackManager = null;
  File dataDirectory = null;
  File archiveDirectory = null;
  File resultDirectory = null;
  ArrayList Tasks = new ArrayList();
  Properties stats = new Properties ( statsDefaults );
  Properties properties = new Properties();
  HttpServer httpServer = null;
  Scheduler scheduler = null;
  ArrayList Rollups = new ArrayList();

  public Project() {
  }
  /**
     Print out some basic status information
  */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append ( "Project: " + title + "\n" );
    buffer.append ( "Statistics: " + stats + "\n" );
    
    buffer.append ( "Table Counts: \n" );
    Connection connection = database.getConnection();
    Statement statement;
    String[] Tables = { "Client", "Submission", "Test", "Result", "Track", "TaskQueue", "CompletedTask" };
    try {
      for ( int i = 0; i < Tables.length; i++ ) {
        statement = connection.createStatement();
        ResultSet rs = statement.executeQuery ( "select count(*) as c from " + Tables[i] );
        rs.next();
        buffer.append ( "\t" + Tables[i] + ": " + rs.getInt ( "c" ) + "\n" );
      }
      connection.close();
    } catch ( Exception e ) {
      logger.error ( title + ": Failed to fetch counts", e );
    }
    buffer.append ( database.toString() + "\n" );
    buffer.append ( servletManager.toString() + "\n" );
    buffer.append ( trackManager.toString() + "\n" );
    return buffer.toString();
  }

  public Properties getStats() { return stats; }
  public Properties getProperties() { return properties; }
  public void setProperties ( Properties p ) { properties = p; }
  public void incrementStatistic ( String key ) { incrementStatistic ( key, 1 ); }
  public void incrementStatistic ( String key, int i ) {
    if ( !stats.containsKey ( key ) ) {
      logger.debug ( title + ": Did not find key: " + key + " in statistics, setting to zero" );
      stats.put ( key, "0" );
    }
    int v = Integer.parseInt ( (String) stats.get ( key ) ) + i;
    stats.put ( key, Integer.toString ( v ) );
  }
  public void incrementStatistic ( String key, double i ) {
    if ( !stats.containsKey ( key ) ) {
      logger.debug ( title + ": Did not find key: " + key + " in statistics, setting to zero" );
      stats.put ( key, "0.0" );
    }
    double v = Double.parseDouble ( (String) stats.get ( key ) ) + i;
    stats.put ( key, Double.toString ( v ) );
  }

  public Server getServer() {
    return dartServer;
  }
  
  public void setBaseDirectory ( String d ) {
    super.setBaseDirectory( d );
    dataDirectory = new File ( d, "Data" );
    archiveDirectory = new File ( d, "Archive" );
    resultDirectory = new File ( d, "Result" );

    if ( !temporaryDirectory.exists() ) {
      temporaryDirectory.mkdir();
      new File ( temporaryDirectory, "Failed" ).mkdir();
    }
    if ( !dataDirectory.exists() ) {
      dataDirectory.mkdir();
    }
    if ( !archiveDirectory.exists() ) {
      archiveDirectory.mkdir();
    }
    if ( !resultDirectory.exists() ) {
      resultDirectory.mkdir();
    }
  }
  public File getResultDirectory () { return resultDirectory; }
  public File getDataDirectory () { return dataDirectory; }
  public File getArchiveDirectory () { return archiveDirectory; }

  public File generateProjectRelativeFileForBinary ( byte[] b, String suffix ) {
    // Only one thread at a time, lock the dataDirectory object
    synchronized ( dataDirectory ) {
      // Generate MD5 hash
      StringBuffer hash = new StringBuffer();
      try {
        MessageDigest algorithm = MessageDigest.getInstance("MD5");
        algorithm.reset();
        algorithm.update ( b );
        byte[] digest = algorithm.digest();
        for (int j = 0; j < digest.length; j++) {
          String hexChar = Integer.toHexString(0xFF & digest[j]);
          while (hexChar.length() < 2) {
            hexChar = '0' + hexChar;
          }
          hash.append ( hexChar );
        }
      } catch ( Exception e ) {
        logger.error ( title + ": Failed to generate MD5 hash", e );
      }
      // logger.debug ( title + ": Found hash: " + hash.toString() );
      String base = hash.substring ( 0, 2 ) + File.separator 
        + hash.substring ( 2, 4 ) + File.separator
        + hash.substring ( 4, 6 ) + File.separator
        + hash.toString();
      // logger.debug ( title + ": Found hash: " + hash.toString() + " Relative File: " + base );
      
      String filename;
      File file;
      int count = -1;
      while ( true ) {
        if ( count == -1 ) {
          filename = base + suffix;
        } else { 
          filename = base + "-" + count + suffix;
        }
        count++;
        file = new File ( getDataDirectory(), filename );
        if ( !file.exists() ) {
          break;
        }
        if ( file.exists() && ( file.length() == b.length ) ) {
          break;
        }
      }
      // Return relative filename
      file = new File ( filename );
      return file;
    }
  }
    
  public void addTask ( String Type, String Schedule, Properties properties ) {
    logger.debug ( title + ": Adding Task: " + Type + " Schedule: " + Schedule + " Properties: " + properties );
    Tasks.add ( new Object[] { Type, Schedule, properties } );
  }


  public ArrayList getRollups () { return Rollups; }

  public void addRollup ( String Type, String Priority, Properties properties ) {
    logger.debug ( title + ": Adding Rollup: " + Type + " Priority: " + Priority + " Properties: " + properties );
    Rollups.add ( new Object[] { Type, Priority, properties } );
  }    

  /** Return the Major, Minor and Patch number in the database in order
   */
  public int[] getDBVersion ( ) throws Exception {
    Connection connection = database.getConnection();
    Statement statement = connection.createStatement();
    int Major, Minor, Patch;
    try {
      ResultSet rs = statement.executeQuery ( "select * from version" );
      if ( !rs.next() ) {
        throw new Exception ( "Version table did not contain any rows!" );
      }
      Major = rs.getInt ( "Major" );
      Minor = rs.getInt ( "Minor" );
      Patch = rs.getInt ( "Patch" );
      logger.debug ( "getDBVersion found Major: " + Major + " Minor: " + Minor + " Patch: " + Patch );
    } finally {
      connection.close();
    }
    return new int[] { Major, Minor, Patch };
  }
    

  /** Verify database exists and is correct version.
   * Throws an exception if the there is no database.
   * Returns false if the DB is out of date.
   * Returns true if the DB has the correct schema.
   */
  public boolean verifyDatabaseVersion () throws Exception {
    // Verify and warn if DB is out of date
    
    Connection connection = database.getConnection();
    Statement statement = connection.createStatement();

    try {
      // Check if Version table exists
      ResultSet tables = connection.getMetaData().getTables ( null, null, "%", null );
      boolean FoundVersion = false;
      while ( tables.next() ) {
        if ( tables.getString ( "TABLE_NAME" ).equalsIgnoreCase ( "version" ) ) {
          FoundVersion = true;
        }
      }

      if ( !FoundVersion ) {
        throw new Exception ( "Version table does not exist in Database MetaData" );
      }

      // Check the version number
      int Major = -1, Minor = -1, Patch = -1;
      int[] version = getDBVersion ();
      Major = version[0]; Minor = version[1]; Patch = version[2];
      if ( Major != DBMajorVersion || Minor != DBMinorVersion ) {
        logger.error ( "Database version is " + Major + "." + Minor 
                       + " expected version is " + DBVersionString );
        return false;
      }
      
    } finally {
      connection.close();
    }
    return true;
  }
    

  /** Helper to do the grunt work of moving from FromMajor.FromMinor to ToMajor.ToMinor
   * Assumes that the database is at FromMajor.FromMinor.  The schema files are of the form
   * dart/Resources/Server/DBUpgrade-"FromMajor"."FromMinor"to"ToMajor"."ToMinor".sql
   * i.e. dart/Resources/Server/DBUpgrade-0.5to0.6.sql
   */  
  void upgradeDatabase ( int FromMajor, int FromMinor, int ToMajor, int ToMinor ) throws Exception {
    int Major = -1, Minor = -1, Patch = -1;
    logger.info ( "Upgrade from " + FromMajor + "." + FromMinor + " to " + ToMajor + "." + ToMinor );
    executeSQL ( new BufferedReader ( new InputStreamReader ( Server.class.getClassLoader().getResourceAsStream( "dart/Resources/Server/DBUpgrade-" + FromMajor + "." + FromMinor + "to" + ToMajor + "." + ToMinor + ".sql" ) ) ) );
    // Make sure it worked...
    int[] version = getDBVersion ();
    Major = version[0]; Minor = version[1]; Patch = version[2];
    if ( Major != ToMajor || Minor != ToMinor ) {
      throw new Exception ( "Failed to update from " + FromMajor + "." + FromMinor + " to " + ToMajor + "." + ToMinor + " Found version " + Major + "." + Minor + " instead" );
    }
    logger.info ( "DB Upgrade complete" );
  }


  /** Update the database for this project
   * For each major and minor number we know about, do the right thing.
   * Verify that the script did what it was supposed to do.
   */
  public void upgradeDatabase () throws Exception {
    // The if statements below must go in order!
    int Major = -1, Minor = -1, Patch = -1;
    int[] version = getDBVersion ();
    Major = version[0]; Minor = version[1]; Patch = version[2];
    logger.debug ( "upgradeDatabase found Major: " + Major + " Minor: " + Minor + " Patch: " + Patch );
    if ( Major == 0 && Minor == 5 ) {
      upgradeDatabase ( 0, 5, 0, 6 );
    }
    /* 0.6 to 1.0 */
    version = getDBVersion ();
    Major = version[0]; Minor = version[1]; Patch = version[2];
    if ( Major == 0 && Minor == 6 ) {
      // Get us to 1.0
      upgradeDatabase ( 0, 6, 1, 0 );
    }
  }

  public void start ( Server server ) throws Exception {
    // start the project
    dartServer = server;
    scheduler = dartServer.getScheduler();

    httpServer = dartServer.getHttpServer();
    logger.info ( title + ": Starting project" );
    try {
      logger.debug ( title + ": Loading statistics" );
      File f = new File ( getBaseDirectory(), "Statistics.txt" );
      if ( f.exists() ) {
        InputStream in = new BufferedInputStream ( new FileInputStream ( f ) );
        stats.load ( in );
      }
    } catch ( Exception e ) {
      logger.error ( title + ": Error loading statistics!", e );
    }
    incrementStatistic ( "Startups" );
    if ( database == null ) {
      throw new Exception ( "Database has not been defined" );
    }

    commandManager.start ( this );
    database.start ( this );
    trackManager.start ( this );
    messengerManager.start ( this );
    listenerManager.start ( this );
    
    // Start up a background job to process the queue
    for ( int i = 0; i < Tasks.size(); i++ ) {
      Object[] o = (Object[]) Tasks.get ( i );
      String Type = null;
      String Schedule = null;
      Properties properties = null;
      Class c = null;
      try { 
        o = (Object[]) Tasks.get ( i );
        Type = (String) o[0];
        Schedule = (String) o[1];
        properties = (Properties) o[2];
        c = Class.forName ( Type );

        if ( Task.class.isAssignableFrom ( c ) ) {
          
          JobDetail detail = new JobDetail ( title + ":" + Type + ":" + i, title, ScheduledTask.class );
          detail.getJobDataMap().put ( "Project", this );
          detail.getJobDataMap().put ( "Type", Type );
          detail.getJobDataMap().put ( "Properties", properties );
          CronTrigger trigger = new CronTrigger ( title + ":" + i, title, Schedule );
          if ( properties.containsKey ( "Description" ) ) {
            trigger.setDescription ( title + ": " + properties.get ( "Description" ) );
          } else {
            trigger.setDescription ( title + ": " + Type );
          }
          scheduler.scheduleJob ( detail, trigger );
          logger.debug ( title + ": Scheduled task " + Type );
        } else {
          logger.error ( title + ": Class: " + Type + " is not a " + Task.class );
        }
      } catch ( Exception e ) {
        logger.error ( title + ": could not find class: " + Type, e );
        continue;
      }
    }


    // Configure the http context for this project
    try {
      logger.debug( title + ": Creating HTTP context with context path /" + title + "/*" );
      HttpContext httpContext = httpServer.getContext("/" + title + "/*" );
      
      logger.debug (title + ": Setting resource base to "+htmlDirectory.getAbsolutePath());
      httpContext.setResourceBase( htmlDirectory.getAbsolutePath() );

      // set of the web application arena (needs to be done before
      // other contexts). Note the mapping is "/Dashboard/*" so that
      // the remainder of the url after /Dashboard/ is passed to the
      // servlet.
      //

      // add the project's Plugins directory to the classpath
      logger.info( title + ": Adding Plugins directory to classpath");
      httpContext.addClassPath(pluginsDirectory.toString());

      // add any jar files in the Plugins directory to the classpath
      File [] jars = pluginsDirectory.listFiles( new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
          }
        } );
      for (int i=0; i < jars.length; i++) {
        if (jars[i].isFile()) {
          logger.info( title + ": Adding " + jars[i].toString() + " to classpath");
          httpContext.addClassPath( jars[i].toString() );
          }
      }    

      // add all the servlets that were specified in Project.xml
      // ResultServlet resultServlet = new ResultServlet();
      // resultServlet.start ( this );
      servletManager.start ( this, httpContext );

      // set up the resource handler for static content
      ResourceHandler handler;
      handler = new ResourceHandler();
      handler.setDirAllowed(true);
      handler.setAcceptRanges(true);
      httpContext.addHandler( handler );

      // setup a resource handler for requests that cannot be
      // satisfied. we should subclass this NotFoundHandler to
      // generate something Dart friendly
      httpContext.addHandler( new NotFoundHandler() );

      // setup an authenticator
      DigestAuthenticator authenticator = new DigestAuthenticator();
      httpContext.setAuthenticator( authenticator );
      
      // Add the Data directory context
      logger.debug( title + ": Creating HTTP context with context path /" + title + "/Data/*" );
      HttpContext dataContext = httpServer.getContext("/" + title + "/Data/*");
      logger.debug (title + ": Setting resource base to "+dataDirectory.getAbsolutePath());
      dataContext.setResourceBase( dataDirectory.getAbsolutePath() );

      handler = new ResourceHandler();
      handler.setDirAllowed(true);
      handler.setAcceptRanges(true);
      dataContext.addHandler ( handler );
    } catch ( Exception e ) {
      logger.error( "Failed to create HTTP context", e );
    }

//     try {
//       NotificationTask mail = new NotificationTask();
//       Properties prop = new Properties();
//       prop.setProperty("to", "millerjv@research.ge.com");
//       prop.setProperty("from", "millerjv@research.ge.com");
//       prop.setProperty("smtpHost", "crdns.research.ge.com");
//       prop.setProperty("smtpPort", "25");
//       prop.setProperty("subject", "Dart notificaton: ");
//       prop.setProperty("content", "Hey jim");
//       mail.execute(this, prop );
//     }
//     catch (Exception e) {
//       logger.info("Failed to mail notification, " + e);
//     }
  }

  public void setTrackManager ( TrackManager t ) { trackManager = t; }
  public TrackManager getTrackManager() { return trackManager; }

  public Scheduler getScheduler () { return scheduler; }

  public void queueTask ( String TaskType, Properties properties, int priority ) {
    Connection connection = getConnection();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String propString = null;
    try {
      properties.store ( out, null );
      propString = out.toString().split ( "\n", 2 )[1];

      // See if this task is alread there, if so, just return...
      JaxorContextImpl session = new JaxorContextImpl ( connection );
      TaskQueueFinderBase finder = new TaskQueueFinderBase ( session );

      QueryParams params = new QueryParams();
      params.add ( new Integer ( priority ) );
      params.add ( TaskType );
      params.add ( propString );
      TaskQueueEntity task;
      if ( finder.asQuery ( "select * from taskqueue where Priority=? and Type=? and Properties=?", params ).list().size() != 0 ) {
        return;
      }
      task = finder.newInstance();
      task.setPriority ( new Integer ( priority ) );
      task.setType ( TaskType );
      task.setProperties ( propString );
      session.commit();
      logger.debug ( title + ": Queued: " + TaskType + "\nProperties: " + propString );
    } catch ( Exception e ) {
      logger.error ( title + ": Failed to add Task: " + TaskType + " to queue with priority: " + priority
                     + " properties:\n" + propString, e );
    } finally {
      try { connection.close(); } catch ( Exception e ) { }
    }
  }

  /**
     Is the Result type a large data type?
     @param type Result type
  */
  public static boolean isLargeDataType ( String type ) {
    return ( !type.equals ( "numeric/integer" )
             && !type.equals ( "numeric/float" )
             && !type.equals ( "numeric/double" )
             && !type.equals ( "text/string" )
             && !type.equals ( "text/url" ) );
  }

  public void initializeDatabase ( ) {
    // See if there is a Schema.sql file in the project directory
    File schema = new File ( baseDirectory, "Schema.sql" );
    if ( !schema.exists() ) {
      logger.error ( title + ": Schema file: " + schema.getPath() + " does not exist" );
      return;
    }
    executeSQL ( schema );
  }

  public Date parseBuildStamp ( String stamp ) {
    // See if we are in UTC
    SimpleDateFormat parse;
    Date date = null;


    try {      
      // logger.debug ( title + ": Parsing build stamp " + stamp );
      String s;
      // Add GMT time zone to stamp.  In Dart1, this was implicit, for proper
      // parsing, make it explicit.
      s = stamp.substring ( 0, 13 ) + " -0000";
      // logger.debug ( title + ": Parsing build stamp " + s );
      parse = new SimpleDateFormat( "yyyyMMdd-HHmm Z" );
      date = parse.parse ( s );
      logger.debug ( title + ": parsed to " + date );
    } catch ( ParseException pe ) {
      // Do nothing
    }

    if ( date == null ) {
      String[] formats = { Container.UTCFormat, // UTC format
                           "MM/dd/yyyy HH:mm:ss", // Cruise control format, not TZ
      };
      for ( int idx = 0; idx < formats.length; idx++ ) {
        try {
          parse = new SimpleDateFormat( formats[idx] );
          date = parse.parse ( stamp );
          if ( date != null ) {
            return date;
          }
        } catch ( ParseException pe ) {
          // Do nothing
          logger.debug ( "failed to parese " + stamp + "\n" + pe );
        }
      }
    }
      
    // Couldn't parse the stamp, put error in log, and return the current time.
    if ( date == null ) {
      date = Calendar.getInstance().getTime();
      logger.error ( this.getTitle() + ": failed to parse BuildStamp " + stamp + " returning 'now' ( " + date + " )" );
    }

    return date;
  }

  public static void generateSchema ( String DBType, File f ) {
    // File f = new File ( baseDirectory, "Schema.sql" );
    if ( f.exists() ) {
      logger.error ( "Error generating schema, file: " + f.getPath() + " exists" );
      return;
    }
    try {
      Writer outTemplate = new BufferedWriter ( new FileWriter ( f ) );
      Server.generateSchema ( DBType, outTemplate );
    } catch ( Exception e ) {
      logger.error ( "Failed to generate schema file: " + f.getPath(), e );
    }
  }


  public void shutdown () throws Exception {
    // Shut everything down...
    try {
      commandManager.shutdown();
    } catch ( Exception e ) {
      logger.error ( title + ": Failed to shutdown CommandManager", e );
    }
    try {
      database.shutdown();
    } catch ( Exception e ) {
      logger.error ( title + ": Failed to shutdown Database", e );
    }
  }

  /** Archive every submission in the database
   */
  public void dumpProject() throws Exception {
    // Create and run the archiver
    Properties props = new Properties();
    props.setProperty ( "ArchiverList", "All" );
    props.setProperty ( "Archiver.All.AgeInDays", "1" );
    props.setProperty ( "Archiver.All.ArchiveLevel", "4" );
    ArchiveTask task = new ArchiveTask ();
    task.execute ( this, props );
  }

  // Refresh the resources for the project.  This will copy the
  // default templates, styles, and icons from the server to the
  // appropriate locations in this project.
  //
  public void refreshResources () {

    // make sure all the project subdirectories exist
    if ( !temporaryDirectory.exists() ) {
      temporaryDirectory.mkdir();
    }
    if ( !dataDirectory.exists() ) {
      dataDirectory.mkdir();
    }
    if ( !htmlDirectory.exists() ) {
      htmlDirectory.mkdir();
    }
    if ( !templatesDirectory.exists() ) {
      templatesDirectory.mkdir() ;
    }
    if ( !pluginsDirectory.exists() ) {
      pluginsDirectory.mkdir() ;
    }

    // Find the default resources on the server
    URL resourceRootURL = Project.class.getClassLoader().getResource( "dart/Resources/Project/Style.css" );
    logger.info ( "Default project resources root path is " + resourceRootURL.toString() );
    FileObject dest = null, src = null, templates = null;
    try {
      FileSystemManager fsManager = VFS.getManager();

      /*
      FileObject t = fsManager.resolveFile ( "jar:file:/projects/mion/Source/Dart/DartServer.jar!/dart/Resources/Project/Style.css" );
      logger.debug ( "Test exists: " + t.exists() );
      
      src = fsManager.resolveFile ( resourceRootURL.toString().replaceAll ( "jar:file:", "jar://" ) ).getParent();
      logger.debug ( "Replaced version:" + src.exists() );
      */
      logger.debug ( "New Resource Root:" + resourceRootURL.toString().replaceAll ( "jar:file:", "jar://" ) );
      logger.debug ( "Resource Root:" + resourceRootURL.toString() );
      src = fsManager.resolveFile ( resourceRootURL.toString() ).getParent();
      logger.debug ( "Src exists: " + src.exists() );

      // copy HTTP resources to the project, excluding the templates
      File destinationDirectory = new File ( htmlDirectory, "Resources" ).getAbsoluteFile();
      dest = fsManager.resolveFile ( destinationDirectory.toString() );
      templates = dest.getParent().getParent().resolveFile ( "Templates" );

      logger.debug ( "src: " + src.toString() );
      logger.debug ( "dest: " + dest.toString() );
      logger.debug ( "templates: " + templates.toString() );

      logger.debug ( "Copy Templates" );
      if ( ! src.resolveFile ( "Templates" ).exists() ) {
        logger.debug ( "Could not find Templates in src" );
      }
      templates.copyFrom ( src.resolveFile ( "Templates" ), new AllFileSelector() );
      logger.debug ( "Copy Resources" );
      dest.copyFrom ( src, new FileSelector () {
          public boolean includeFile ( FileSelectInfo info ) throws Exception {
            return !info.getFile().getName().getBaseName().equals ( "Templates" );
          }
          public boolean traverseDescendents ( FileSelectInfo info ) throws Exception {
            return !info.getFile().getName().getBaseName().equals ( "Templates" );
          }
        } );
    } catch ( Exception e ) {
      logger.error ( title + ": Failed to refresh resources", e );
    }

  }

  /**
   * Creates a new project give the directory.
   * Create a new project, configure the defaults from
   * dart/Resources/Server/DartDefault.xml.
   * @param path Path of directory to create.  If existing, throws and error.
   * @param db Type of database to use, if not null, generate Schema.sql file
   */
  public static void createProject ( String path, String db, String templateFile ) throws Exception {
    // Create the new directory, error if already existing.
    File dir = new File ( path ).getAbsoluteFile();
    if ( dir.exists() ) {
      logger.error ( "Directory: " + path + " exists, can not create new project" );
      throw new Exception ( "Directory: " + path + " exists, can not create new project" );
    }
    String name = dir.getName();
    logger.info ( "Creating new Project: " + path + " " + name + " " + db );
    Writer outTemplate = null;
    try {
      logger.debug ( "Creating directory " + dir.toString() );
      dir.mkdirs();

      // Get a FreeMarker configuration engine
      Configuration cfg = new Configuration();
      Template template;
      if ( templateFile == null ) {
        cfg.setClassForTemplateLoading ( Server.class, "/" );
        template = cfg.getTemplate ( "dart/Resources/Server/DartDefault.xml" );
      } else {
        File f = new File ( templateFile );
        cfg.setDirectoryForTemplateLoading ( f.getCanonicalFile().getParentFile() );
        template = cfg.getTemplate ( f.getName() );
      }
      outTemplate = new BufferedWriter ( new FileWriter ( new File ( dir, "Project.xml" ) ) );
      Map root = new HashMap();
      root.put ( "ProjectName", name );
      root.put ( "ProjectDirectory", dir.toString() );
      if ( db != null ) {
        root.put ( "Type", db.toLowerCase() );
      } else {
        root.put ( "Type", "derby" );
      }
      template.process ( root, outTemplate );
      outTemplate.flush();

      if ( db != null ) {
        // Writer out = new BufferedWriter ( new FileWriter ( new File ( dir, "Schema.sql" ) ) );
        generateSchema ( db, new File ( dir, "Schema.sql" ) );
      }

    } catch ( Exception e ) {
      logger.error ( "Error creating default project", e );
    } finally {
      if ( outTemplate != null ) {
        try { 
          outTemplate.close();
        } catch ( IOException e ) { 
          logger.error ( "Failed to close output stream", e );
        }
      }
    }
  }

  /**
   * Load an individual project.
   * Loads the project given using Digestor to create all objects.
   * DartObjectCreationRules.xml gives rules to Digestor for object
   * creation.
   * @return A valid project
   */
  public static Project loadProject ( String inPath ) {
    // See if this is a file or directory, if directory, look for the file Project.xml
    File path = new File ( inPath );
    if ( path.isDirectory () ) {
      path = new File ( path, "Project.xml" );
    }
    Project project = null;
    try {
      // Default rules
      URL rules = Server.class.getClassLoader().getResource ( "dart/Resources/Server/DartObjectCreationRules.xml" );
      Project.logger.debug ( "Found DartObjectCreationRules.xml at: " + rules );
      // Try to create the project
      Digester digester = new Digester();
      digester.setRules ( new ExtendedBaseRules() );
      digester.addRuleSet ( new FromXmlRuleSet ( rules ) );

      Project.logger.info ( "Processed rules, starting to parse" );
      project = (Project) digester.parse ( path );
      Project.logger.info ( "Parsed to create a project" );
    } catch ( Exception e ) {
      Project.logger.error ( "Failed to create project", e );
    }

    return project;
  }
}

/*
 * $Log:$
 */




