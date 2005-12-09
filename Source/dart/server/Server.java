package dart.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.DirectSchedulerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
  import qed.server.*;

/**
   Manager for multiple Projects
   The Server is responsible for setting up the XML-RPC
   connection, the Scheduler, loading and starting all Projects.
   @author Daniel Blezek
   @version $Revision: 1.2 $
*/ 
public class Server extends Container
{
  static Logger logger = Logger.getLogger ( Server.class );   
  HttpServer httpServer = null;
  Scheduler scheduler = null;
  static HashMap projects = new HashMap();
  static HashMap qeds = new HashMap();
  static HashMap servers = new HashMap();
  HashMap projectNames = new HashMap();
  int httpPort = 8081;
  int schedulerThreadPoolSize = 10;

  boolean initializeProjectDB = false;
  boolean refreshProjectResources = false;
  boolean refreshServerResources = false;
  boolean dumpProject = false;
  
  /**
   * Do we initialize the projects when starting
   * @param b Initialize or not
   */
  public void setInitializeProjectDB ( boolean b ) { initializeProjectDB = b; }

  /**
   * Do we refresh the project resources when starting
   * @param b Refresh or not
   */
  public void setRefreshProjectResources ( boolean b ) { refreshProjectResources = b; }
  public void setDumpProject ( boolean d ) { dumpProject = d; };

  /**
   * Do we refresh the server resources when starting
   * @param b Refresh or not
   */
  public void setRefreshServerResources ( boolean b ) { refreshServerResources = b; }
  
  /**
     Get the scheduler for this Server
     @return Scheduler used in this Server
  */
  public Scheduler getScheduler() { return scheduler; }

  /**
     Get the HttpServer for this Server
     @return HttpServer used in this Server
  */
  public HttpServer getHttpServer() { return httpServer; }

  /**
     Set the Http Port
     @param p Integer port number
  */
  public void setHttpPort ( String p ) { httpPort = Integer.parseInt ( p ); }

  /**
     Lookup the project by name
     @param name Name of the project to find
  */
  static public Project getProject( String name ) {
    return (Project) projects.get(name);
  }
  /**
     Lookup the qed by name
     @param name Name of the project to find
  */
  static public QED getQED( String name ) {
    return (QED) qeds.get(name);
  }
  /**
     Lookup the Server by name
     @param name Name of the Server to find
  */
  static public Server getServer( String name ) {
    return (Server) servers.get(name);
  }
  
  /**
     Shutdown all services and the projects
  */
  public void doShutdown () {
    // Shut down the server
    // Shut down web server
    logger.warn ( "Starting shutdown of Dart Server" );
    // First stop the scheduler
    try {
      logger.warn ( "Waiting for Scheduler to finish running jobs" ); 
      scheduler.shutdown ( true );
      logger.warn ( "Scheduler shutdown" ); 
    } catch ( Exception e ) {
      logger.error ( "Failed to shutdown Scheduler", e );
    }

    // Stop each project
    Iterator i = projects.values().iterator();
    while ( i.hasNext() ) {
      Project p = (Project) i.next();
      try {
        logger.warn ( "Shutting down project " + p.getTitle() );
        p.shutdown();
      } catch ( Exception e ) {
        logger.error ( "Failed to shutdown: " + p.getTitle(), e );
      }
    }

    // Shutdown jetty
    try {
      logger.warn( "Waiting for HTTP server shutdown" );
      httpServer.stop(true);
      logger.warn( "HTTP server shutdown" );
    } catch ( Exception e ) {
      logger.error( "Failed to shutdown HTTP server" );
    }

    // Shutdown the server database
    try {
      getDatabase().shutdown();
    } catch ( Exception e ) {
      logger.error ( getTitle() + ": Failed to shutdown Database", e );
    }
    
    logger.warn ( "Dart Server Shutdown" );
  }

  /**
     Constructor.  Does nothing.
  */
  public Server () {
    // nothing to do
  }

  /**
   * Add a project to the server
   */
  public void addProject ( String path ) {
    projectNames.put ( path, path );
  }

  public void clearProjects () {
    projectNames.clear();
  }
    
  /**
     Start the Server
   */
  public void start() { start ( true ); }
  /**
     Start the Server
     This method may or may not really start the server.  Primarly
     used for testing.
     @param reallyStart Start the server, used for testing
   */
  public void start ( boolean reallyStart ) {
    // Make call to allow the Dart server to run in a headless
    // environment. This is needed for running JFreeCharts on a
    // headless linux machine. Since the DartServer does not need a
    // gui, we might as well always use a headless system.
    System.setProperty("java.awt.headless","true");

    // Refresh the server resources if necessary
    if (refreshServerResources) {
      refreshResources();
    }

    // Start database
    try {
      getDatabase().start ( this );
    } catch ( Exception e ) {
      logger.error ( "Failed to start database", e );
    }

    // Initialize the scheduler
    try {
      logger.info ( "Initializing Scheduler" );
      DirectSchedulerFactory.getInstance().createVolatileSchduler ( schedulerThreadPoolSize );
      scheduler = DirectSchedulerFactory.getInstance().getScheduler();
      logger.info ( "Scheduler initialized" );
    } catch ( Exception e ) {
      logger.error ( "Failed to initialize scheduler: ", e );
      return;
    }
    
    // Create and HTTP and Servlet server, only one HTTP server for
    // the entire DartServer.  Each project will have its own context
    // and webapplication/servlet area. HTTP server will be started later.
    try {
      logger.info( "Initializing HTTP server" );
      httpServer = new HttpServer();
      httpServer.setStatsOn ( true );
      SocketListener listener = new SocketListener();
      listener.setPort( httpPort );
      httpServer.addListener(listener);
      logger.info( "HTTP server initialized" );

      // create a default context for the entire dart server
      HttpContext defaultContext = httpServer.getContext("/");
      defaultContext.setResourceBase( htmlDirectory.getAbsolutePath() );

      // add a special handler for page not found
      DefaultNotFoundHandler defaultNotFoundHandler = new DefaultNotFoundHandler();
      defaultNotFoundHandler.setDartServer( this );
      defaultContext.addHandler( defaultNotFoundHandler );

      
//       logger.info( "Establishing JDBC user realm" );
//       JDBCUserRealm realm = new JDBCUserRealm("Dart");
//       URL realmConfigurationFile = DartServer.class.getClassLoader().getResource( "dart/Resources/Server/realm.properties" );
//       realm.loadConfig( realmConfigurationFile.getPath() );
//       realm.connectDatabase();
//       httpServer.addRealm(realm);
      
      logger.info( "Creating security constraint" );
      SecurityConstraint security = new SecurityConstraint();
      security.setAuthenticate( false);
      //security.addRole( "Dart.Administrator" );
      security.setName( "Dart.Administrator" );

      // HttpContext context = httpServer.getContext( "/TestServer/*" );
      // context.addSecurityConstraint( "/", security );
//       context.addSecurityConstraint( "/TestServer/*", security );
//       context.setRealmName(realm.getName());
        
    } catch ( Exception e ) {
      logger.error( "Failed to initialize HTTP server", e );
      System.exit ( 1 );
    }

    try {
      // Start the Server servelet manager
      HttpContext httpContext = httpServer.getContext("/DartServer/*" );
      httpContext.setResourceBase( htmlDirectory.getAbsolutePath() );
      // add all the servlets that were specified in Server.xml
      servletManager.start ( this, httpContext );
      // add a resource manager for static content
      ResourceHandler handler;
      handler = new ResourceHandler();
      handler.setDirAllowed(true);
      handler.setAcceptRanges(true);
      httpContext.addHandler( handler );
    } catch ( Exception e ) {
      logger.error( "Failed to initialize ServletManager", e );
      System.exit ( 1 );
    }
      
    // Start the CommandManager
    try {
      commandManager.start ( this );
    } catch ( Exception e ) {
      logger.error( "Failed to start CommandManager", e );
      System.exit ( 1 );
    }

    // In turn, load each project, do any house keeping it may need and start it
    Iterator i = projectNames.keySet().iterator();
    while ( i.hasNext() ) {
      String name = (String) i.next();
      boolean isOK = false;
      try {
        Project p = Project.loadProject ( name );
        if ( p != null ) {
          projects.put ( p.getTitle(), p );
          p.start( this );
          if ( initializeProjectDB ) {
            p.initializeDatabase ( );
          }
          if ( refreshProjectResources ) {
            p.refreshResources ();
          }
          if ( dumpProject ) {
            p.dumpProject();
          }
          isOK = true;
        }
      } catch ( Exception e ) {
        logger.error ( "Error starting project " + name, e );
      }
      if ( isOK == false ) {
        try {
          // Try for a QED
          logger.info ( "Trying to load a QED" );
          QED qed = QED.loadQED ( name );
          if ( qed != null ) {
            qeds.put ( qed.getTitle(), qed );
            qed.start ( this );
            if ( initializeProjectDB ) {
              qed.initializeDatabase ( );
            }
            if ( refreshProjectResources ) {
              qed.refreshResources ();
            }
            isOK = true;
          }
        } catch ( Exception e ) {
          logger.error ( "Error starting qed " + name, e );
        }
      }
    }

    
    // Bow out before starting everything up
    // if ( !reallyStart ) { return; }
    // Set up the scheduler, only one is created for the entire DartServer
    try {
      logger.info ( "Starting Scheduler" );
      scheduler.start();
      logger.info ( "Scheduler started" );
    } catch ( Exception e ) {
      logger.error ( "Failed to start scheduler", e );
      System.exit ( 1 );
    }

    // Start the HTTP server. All contexts need to be added prior to
    // starting the server.
    try {
      logger.info( "Starting HTTP server" );
      httpServer.start();
      logger.info( "HTTP server started" );
    } catch ( Exception e ) {
      logger.error ( "Failed to start HTTP server", e );
      System.exit( 1 );
    }
              
    // Setup and start the XMLRPC server
    // Add the administrator command
    // xmlrpcServer.registerProxyService( new DartCommandHandler(), "Admin" );
    // new Thread ( new XmlRpcServerHelper ( xmlrpcServer, xmlrpcPort ) ).start();
    StringBuffer b = new StringBuffer ( "Dart Server started with Projects: " );
    Iterator pi = projects.keySet().iterator();
    while ( pi.hasNext() ) {
      b.append ( (String)pi.next() + " " );
    }
    logger.info ( b.toString() );
  }

  /**
     Generates the Schema.sql file for a database type
     Uses FreeMarker to generate Schema.sql based on the database
     type, saving output to the given Writer.
     @param DBType Type of database, currently supported are generic, postgres, and derby.
     @param out Where to write the Schema text
  */
  static public void generateSchema ( String DBType, Writer out ) {
    Server.generateSchema ( DBType, out, "dart/Resources/Server/Schema.sql" );
      }
  static public void generateSchema ( String DBType, Writer out, String SchemaPath ) {
    try {
      // Generate and write schema into project directory
      Configuration cfg = new Configuration();
      cfg.setClassForTemplateLoading ( Server.class, "/" );
      Template template = cfg.getTemplate ( SchemaPath );
      Map root = new HashMap();
      root.put ( "Type", DBType.toLowerCase() );
      if ( DBType.toLowerCase().equals ( "generic" ) || DBType.toLowerCase().equals ( "postgres" ) ) {
        logger.debug ( "Found generic or Postgres" );
        root.put ( "auto", "bigserial primary key" );
        root.put ( "now", "'now'" );
      } else if ( DBType.toLowerCase().equals ( "derby" ) ) {
        logger.debug ( "Found derby" );
        root.put ( "auto", "bigint generated always as identity" );
        root.put ( "now", "CURRENT_TIMESTAMP" );
      }
      template.process ( root, out );
      out.flush();
    } catch ( Exception e ) {
      logger.error ( "Faild to generate schema\n", e );
      return;
    }
  }

  /**
     Creates a server directory
     Create a new server, 
     @param path Path of directory to create.  If existing, throws and error.
     @param db Type of database to use, if not null, generate Schema.sql file
  */
  public static void createServer ( String path, String db ) throws Exception {
    
    // Create the new directory, error if already existing.
    File dir = new File ( path ).getAbsoluteFile();
    if ( dir.exists() ) {
      logger.error ( "Directory: " + path + " exists, can not create new server" );
      throw new Exception ( "Directory: " + path + " exists, can not create new server" );
    }
    String name = dir.getName();
    logger.info ( "Creating new Server: " + path + " " + name + " " + db );
    Writer outTemplate = null;
    try {
      logger.debug ( "Creating directory " + dir.toString() );
      dir.mkdirs();

      // Get a FreeMarker configuration engine
      Configuration cfg = new Configuration();
      cfg.setClassForTemplateLoading ( Server.class, "/" );
      Template template = cfg.getTemplate ( "dart/Resources/Server/DartServerDefault.xml" );
      outTemplate = new BufferedWriter ( new FileWriter ( new File ( dir, "Server.xml" ) ) );
      Map root = new HashMap();
      root.put ( "ServerName", name );
      root.put ( "ServerDirectory", dir.toString() );
      if ( db != null ) {
        root.put ( "Type", db.toLowerCase() );
      } else {
        root.put ( "Type", "derby" );
      }
      template.process ( root, outTemplate );
      outTemplate.flush();

      if ( db != null ) {
        Writer out = new BufferedWriter ( new FileWriter ( new File ( dir, "ServerSchema.sql" ) ) );
        generateServerSchema ( db, out );
      }

    } catch ( Exception e ) {
      logger.error ( "Error creating server", e );
    } finally {
    }

  }

  /**
     Generates the ServerSchema.sql file for a database type
     Uses FreeMarker to generate ServerSchema.sql based on the database
     type, saving output to the given Writer.
     @param DBType Type of database, currently supported are generic, postgres, and derby.
     @param out Where to write the schema text
  */
  static public void generateServerSchema ( String DBType, Writer out ) {
    try {
      // Generate and write schema into project directory
      Configuration cfg = new Configuration();
      cfg.setClassForTemplateLoading ( Server.class, "/" );
      Template template = cfg.getTemplate ( "dart/Resources/Server/ServerSchema.sql" );
      Map root = new HashMap();
      root.put ( "Type", DBType.toLowerCase() );
      if ( DBType.toLowerCase().equals ( "generic" ) || DBType.toLowerCase().equals ( "postgres" ) ) {
        logger.debug ( "Found generic or Postgres" );
        root.put ( "auto", "serial" );
        root.put ( "now", "'now'" );
      } else if ( DBType.toLowerCase().equals ( "derby" ) ) {
        logger.debug ( "Configuring Dart server using derby" );
        root.put ( "auto", "int generated always as identity" );
        root.put ( "now", "CURRENT_TIMESTAMP" );
      }
      template.process ( root, out );
      out.flush();
    } catch ( Exception e ) {
      logger.error ( "Faild to generate schema\n", e );
      return;
    }
  }

  /**
     Load server.
     Loads the server given using Digestor to create all objects.
     DartObjectCreationRules.xml gives rules to Digestor for object
     creation.
     @return A valid server
  */
  public static Server loadServer ( String inPath ) {
    logger.info( "Loading Dart server from " + inPath );
    
    // See if this is a file or directory, if directory, look for the file Server.xml
    File path = new File ( inPath );
    if ( path.isDirectory () ) {
      path = new File ( path, "Server.xml" );
    }

    Server server = null;
    try {
      // Default rules
      URL rules = Server.class.getClassLoader().getResource ( "dart/Resources/Server/DartObjectCreationRules.xml" );
      logger.debug ( "Found DartObjectCreationRules.xml at: " + rules );
      // Try to create the project
      Digester digester = new Digester();
      digester.setRules ( new ExtendedBaseRules() );
      digester.addRuleSet ( new FromXmlRuleSet ( rules ) );
      
      logger.info ( "Processed rules, starting to parse" );
      server = (Server) digester.parse ( path );
      logger.info ( "Parsed to create server" );
      servers.put ( server.getTitle(), server );
    } catch ( Exception e ) {
      logger.error ( "Failed to create server", e );
    }

    return server;
  }

  /**
     Initialize the Database for this project
     Read the ServerSchema.sql file in the Base Directory and 
     execute it.
  */
  public void initializeDatabase () {
    logger.info( getTitle() + ": initializing server database" );
    
    // See if there is a ServerSchema.sql file in the dart server directory
    File schema = new File ( getBaseDirectory(), "ServerSchema.sql" );
    if ( !schema.exists() ) {
      logger.error ( getTitle() + ": Schema file: " + schema.getPath() + " does not exist" );
      return;
    }
    executeSQL ( schema );
    
  }

  /**
     Return the status of this Server
     @return Status of the server
  */
  public String getStatus () {
      StringWriter b = new StringWriter();
      PrintWriter out = new PrintWriter ( b );
      out.println ( "DartServer status\n" );
      try {
        out.println ( "Project Info" );
        Iterator p = projects.values().iterator();
        while ( p.hasNext() ) {
          Project project = (Project) p.next();
          out.println ( project.toString() );
        }

        DecimalFormat format = new DecimalFormat ( "0.00" );
        out.println ( "Memory: " + format.format ( Runtime.getRuntime().totalMemory() / 1048576.0 ) + "M / " + format.format ( Runtime.getRuntime().maxMemory() / 1048576.0 ) + "M\n");

        out.println ( "HTTP Server: " );
        out.println ( "\tConnections: " + httpServer.getConnections() );
        out.println ( "\tAverage Connection Duration: " + httpServer.getConnectionsDurationAve() );
        out.println ( "\tMax Connection Duration: " + httpServer.getConnectionsDurationMax() );
        out.println ( "\tOpen Connections: " + httpServer.getConnectionsOpen() );
        out.println ( "\tMax Open Connections: " + httpServer.getConnectionsOpenMax() );
        out.println ( "\tAve Connection Requests: " + httpServer.getConnectionsRequestsAve() );
        out.println ( "\tMax Connection Requests: " + httpServer.getConnectionsRequestsMax() );
        out.println ( "\tConnections: " + httpServer.getConnections() );
        out.println ( "Scheduler: " + scheduler.getMetaData().getSummary() );
        out.println ( "Currently executing: " + scheduler.getCurrentlyExecutingJobs().size() );
        out.println ( "Paused: " + scheduler.getPausedTriggerGroups() );
        out.println ( "Triggers" );
        String[] groups = scheduler.getTriggerGroupNames();
        for ( int i = 0; i < groups.length; i++ ) {
          String[] names = scheduler.getTriggerNames ( groups[i] );
          for ( int j = 0; j < names.length; j++ ) {
            Trigger trigger = scheduler.getTrigger ( names[j], groups[i] );
            out.println ( trigger.toString() );
          }
        }
      } catch ( Exception e ) {
        logger.error ( "Failed to get scheduler status", e );
      }
      return b.toString();
    }

  // Refresh the resources for the server.  This will copy the
  // default templates, styles, and icons to the appropriate locations
  // in this server.
  //
  public void refreshResources () {

    // make sure all the project subdirectories exist
    if ( !temporaryDirectory.exists() ) {
      temporaryDirectory.mkdir();
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
    URL resourceRootURL = Server.class.getClassLoader().getResource( "dart/Resources/Server/Style.css" );
    logger.info ( "Default server resources root path is " + resourceRootURL.toString() );
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

      // copy HTTP resources to the server, excluding the templates
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
  
}


/*
 * Log: $Log: Server.java,v $
 * Log: Revision 1.2  2005/02/20 22:04:38  blezek
 * Log: All tests working with new Container, Server, Project architecture
 * Log:
 * Log: Revision 1.1  2005/02/20 18:43:56  blezek
 * Log: Refactored into Server.java, DartServer only processes command line arguments
 * Log:
 */
