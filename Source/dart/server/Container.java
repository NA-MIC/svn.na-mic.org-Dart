package dart.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.mortbay.http.HttpServer;

/**
 * Base class for Dart objects that have a database and an http context
 * @author Jim Miller
 * @version $Revision:$
 */
public class Container {
  static Logger logger = Logger.getLogger ( Container.class );   

  public static String UTCFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  protected String title = "Default";
  protected Database database = null;
  protected ServletManager servletManager = null;
  protected File baseDirectory = null;
  protected File temporaryDirectory = null;
  protected File htmlDirectory = null;
  protected File pluginsDirectory = null;
  protected File templatesDirectory = null;
  int uniqueFileIndex = 0;
  protected Server dartServer = null;
  protected CommandManager commandManager = null;
  
  /**
   * Default (empty) constructor
   */
  public Container() {
  }

  /** 
   * Set/Get the title of this container
   * @param t Title to set
   */
  public void setTitle ( String t ) { title = t; logger.debug ( "title: " + title ); }
  public String getTitle () { return title; }

  /**
   * Set the base directory for this object.
   * @param d Director name
   */
  public void setBaseDirectory ( String d ) {
    baseDirectory = new File ( d );
    temporaryDirectory = new File ( d, "Temporary" );
    htmlDirectory = new File ( d, "HTML" );
    pluginsDirectory = new File( d, "Plugins" );
    templatesDirectory = new File( d, "Templates" );

    if ( !temporaryDirectory.exists() ) {
      temporaryDirectory.mkdir();
    }
    if ( !htmlDirectory.exists() ) {
      htmlDirectory.mkdir();
    }
    if ( !pluginsDirectory.exists() ) {
      pluginsDirectory.mkdir() ;
    }
    if ( !templatesDirectory.exists() ) {
      templatesDirectory.mkdir() ;
    }
  }

  /**
   * Get the different directories used by this Container
   */
  public File getBaseDirectory () { return baseDirectory; }
  public File getTemporaryDirectory () { return temporaryDirectory; }
  public File getHtmlDirectory () { return htmlDirectory; }
  public File getPluginsDirectory () { return pluginsDirectory; }
  public File getTemplates () { return templatesDirectory; }

  /**
   * Generate a unique file name in the temporary directory
   * @param suffix Suffix of the file to create
   * @return A unique file in from the Temporary directory
   */
  public File getUniqueFileInTemporaryDirectory ( String suffix ) {
    synchronized ( temporaryDirectory ) {
      while ( true ) {
        File f = new File ( temporaryDirectory, "File" + uniqueFileIndex + suffix );
        if ( !f.exists() ) {
          return f;
        }
        uniqueFileIndex++;
      }
    }
  }

  /**
   * Replace any bad filename characters in the string and return
   * @param name Filename
   * @return Filename with any characters bad for filenames replaced with "_"
   */
  public static String generateSafeFileName ( String name ) {
    // Replace anything that is not in the list
    String n = name.replaceAll ( "[^0-9a-zA-Z-_+.:]", "_" );
    // Windows doesn't like ":" which we have in our UTC timecode
    return n.replaceAll ( ":", "." );
  }

  /**
   * Get/Set the database for this Container
   * @param d Database to set
   */
  public void setDatabase ( Database d ) { database = d; }
  public Database getDatabase () { return database; }

  /**
   * Get/Set the CommandManager for this container
   * @param c CommandManager to set
   */
  public void setCommandManager ( CommandManager c ) { commandManager = c; }
  public CommandManager getCommandManager () { return commandManager; }

  /**
   * Get a connection from the database.
   * @return A JDBC Connection object
   */
  public Connection getConnection() { return database.getConnection(); }

  /**
   * Get the webserver associated with this Container
   * @return Web server object
   */
  public HttpServer getHttpServer () { return dartServer.getHttpServer(); }

  /**
   * Set the servelet for this container to use
   * @param m Serverlet Manager to use
   */
  public void setServletManager ( ServletManager m ) { servletManager = m; }

  /**
   * Read and execute the SQL contained in a file.
   * Read the input file line by line, until a ";" is found.  At this
   * point, execute the SQL statement.  In SQL, "--" indicates a
   * comment all text to the end of the line is ignored.  Any errors
   * are reported, but ignored.
   * @param schema File containing the SQL to be executed.
   */
  public void executeSQL ( File schema ) {
    // Read and execute
    StringWriter writer = new StringWriter();
    PrintWriter w = new PrintWriter ( writer );
    try {
      BufferedReader reader = new BufferedReader ( new FileReader ( schema ) );
      String b = null;
      while ( true ) {
        b = reader.readLine();
        if ( b == null ) { 
          break;
        }
        w.print ( b.replaceAll ( "--.*", " " ) );
      }
      reader.close();
    } catch ( Exception e ) {
      logger.error ( getTitle() + ": Failed to open schema\n", e );
      return;
    }
    
    // Execute each command
    Connection connection = getConnection();
    Statement statement = null;
    try {
      statement = connection.createStatement();
      StringTokenizer tokenizer = new StringTokenizer ( writer.toString(), ";" );
      while ( tokenizer.hasMoreTokens() ) {
        String s = tokenizer.nextToken().replace ( '\n', ' ' ).trim();
        if ( s.length() != 0 ) {
          logger.debug ( getTitle() + ": Found statement: " + s );
          statement.execute ( s );
        }
      }
    } catch ( Exception e ) {
      logger.error ( getTitle() + ": Error initializing the database\n", e );
    } finally {
      try { connection.close(); } catch ( Exception e ) { }
    }
  }

}



/*
 * $Log:$
 */
