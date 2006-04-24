package dart;

import java.io.*;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.vfs.*;

import dart.server.Container;
import dart.server.Server;
import dart.server.Project;
//import qed.server.*;
/**
   Manager for multiple Projects
   The DartServer is responsible for setting up the XML-RPC
   connection, the Scheduler, loading and starting all Projects.
   @author Daniel Blezek
   @version $Revision: 1.30 $
*/ 
public class DartServer extends Container
{
  static Logger logger = Logger.getLogger ( DartServer.class );   
  CommandLine cmd = null;
  static String helpString = "DartServer [options] Server.xml <Project0.xml> <Project1.xml> ... <ProjectN.xml>";
  static Server server;


  /**
   * Process command line arguments and do specified commands
   */
  public static void main( String[] args ) {

    server = null;
    CommandLine cmd = DartServer.parseCommandLine( args );

    // Help command
    if ( cmd.hasOption ( "h" ) ) {
      new HelpFormatter().printHelp ( helpString, DartServer.getOptions() );
      System.exit ( 0 );
    }

    // Log4j configuration
    URL logConfigurationFile = DartServer.class.getClassLoader().getResource ( "dart/Resources/Server/log4j.properties" );
    if ( cmd.hasOption ( "l" ) ) {
      try {
        logConfigurationFile = new File ( cmd.getOptionValue ( "l" ) ).toURL();
      } catch ( Exception e ) {
        logger.error ( "Error in log4j configuration file name", e );
        System.exit ( 1 );
      }
    }
    PropertyConfigurator.configure ( logConfigurationFile );
    logger.info ( "Starting DartServer version " + dart.server.Version.getVersionString() + " db version " + dart.server.Version.getDBVersionString() );
    logger.info ( "Initialized log4j from " + logConfigurationFile.toString() ); 

    // Create a project
    if ( cmd.hasOption ( "c" ) ) {
      try {
        // Create a project
        Project.createProject ( cmd.getOptionValue ( "c" ), cmd.getOptionValue ( "d", "derby" ), cmd.getOptionValue ( "t", null ) );
        System.exit(0);
      } catch ( Exception e ) {
        logger.error( "Failed to create Project: ", e);
        System.exit(1);
      }
    }
        
    // Create a QED project
//     if ( cmd.hasOption ( "q" ) ) {
//       try {
//         // Create a project
//         QED.createQED ( cmd.getOptionValue ( "q" ), cmd.getOptionValue ( "d", "derby" ) );
//         System.exit(0);
//       } catch ( Exception e ) {
//         logger.error( "Failed to create QED: ", e);
//         System.exit(1);
//       }
//     }

    // Create user database that is shared among all projects on server
    if ( cmd.hasOption( "k" ) ) {
      try {
        // Deploy a Dart server
        Server.createServer( cmd.getOptionValue( "k", "DartServer"), cmd.getOptionValue("d", "derby") );
        System.exit(0);
      } catch (Exception e) {
        logger.error( "Failed to create user database: ", e);
        System.exit(1);
      }
    }

    // Load an existing server from the specified configuration file
    String[] otherargs = cmd.getArgs();
    if ( otherargs.length == 0 ) {
      new HelpFormatter().printHelp( helpString, DartServer.getOptions() );
      System.exit( 0 );
    }
    // load the server specified
    server = Server.loadServer( otherargs[0] );
    
    // Commandline projects override XML configuration file
    if ( otherargs.length > 1 ) {
      server.clearProjects();
      for ( int i = 1; i < otherargs.length; i++ ) {
        server.addProject( otherargs[i] );
      }
    }

    logger.info ( "Server name: " + server.getTitle() );
    logger.info ( "Server directory: " + server.getBaseDirectory() );

    // Initialize the server database
    try {
      server.getDatabase().start( server );
      if ( cmd.hasOption ( "j" ) ) {
        server.initializeDatabase ( );
        System.exit( 0 );
      }
    } catch (Exception e) {
      logger.error ("Error initializing Dart server database" );
    }

    // call some method on the dart server to continue processing
    if ( cmd.hasOption ( "i" ) ) {
      server.setInitializeProjectDB ( true );
    }
    if ( cmd.hasOption ( "r" ) ) {
      server.setRefreshProjectResources ( true );
    }
    if ( cmd.hasOption ( "a" ) ) {
      server.setDumpProject ( true );
    }
    if ( cmd.hasOption( "R" ) ) {
      server.setRefreshServerResources( true );
    }
    if ( cmd.hasOption( "u" ) ) {
      server.setUpgradeProjectDB( true );
    }
    try {
      server.start();
    } catch ( Exception e ) {
      logger.fatal ( "Failed to start DartServer see previous messages" );
      System.exit ( 0 );
    }
  }


  /**
   * Get the options that will be used for command line parsing
  */
  public static Options getOptions () {
    // Configure the command line options
    Options options = new Options();

    options.addOption ( "h", "help", false, "Print help message" );
    options.addOption ( "r", "refresh", false, "Refresh project resources" );
    options.addOption ( "a", "archive", false, "Archive the project" );
    options.addOption ( "R", "refreshServer", false, "Refresh server resources" );
    options.addOption ( "l", "logconfiguration", true, "File to configure log4j from, defaults are used if not present" );
    options.addOption ( "c", "create", true, "Create a new project in the directory specified" );
    options.addOption ( "d", "database", true, "At project creation time, configure the Schema.sql file for generic, Postgres, Derby" );
    options.addOption ( "i", "initialize", false, "Initialize the database from the Schema.sql file in the project directory" );
    options.addOption ( "k", "createserver", true, "Create a new server in the directory specified" );
    options.addOption ( "j", "initializeserver", false, "Initialize the database from the ServerSchema.sql file in the dart server directory" );
    //    options.addOption ( "q", "createqed", true, "Create a new QED in the directory specified" );
    options.addOption ( "t", "projecttemplate", true, "Create a new Project using the specified default template: dart/Resources/Server/DartDefault.xml in the jar file is the default" );
    options.addOption ( "u", "upgradeprojectdb", false, "Update all Project's databases to the lastest version" );
    // options.addOption ( "n", "name", true, "New project name" );
    // options.addOption ( "p", "port", true, "Port to run the XML-RPC server on, 8080 is default" );
    // options.addOption ( "q", "httpPort", true, "Port to run the http server on, 8081 is default" );
    // options.addOption ( "t", "threadpool", true, "Number of threads to allocate to the scheduler thread pool, 10 is default" );
    // options.addOption ( "s", "schema", true, "Generate SQL schema, must specify one of: generic, Postgres, Derby");
    // options.addOption ( "m", "MD5", true, "Calculate and print MD5 hash" );
    // options.addOption ( "s", "servername", true, "New server name" );

    return options;
  }    

  /**
   * Parse the command line arguments
   */
  public static CommandLine parseCommandLine ( String[] args ) {

    // Get the command line options
    Options options = DartServer.getOptions();

    // Parse the command line
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse ( options, args );
    } catch ( Exception e ) {
      System.out.println ( "Failed to parse commandline: "+e+"\n" );
      new HelpFormatter().printHelp ( helpString, options );
      System.exit ( 1 );
    }

    // return the parsed command line
    return cmd;
  }    
}

/*
 * Log: $Log: DartServer.java,v $
 * Log: Revision 1.30  2005/02/20 22:04:38  blezek
 * Log: All tests working with new Container, Server, Project architecture
 * Log:
 * Log: Revision 1.29  2005/02/20 18:42:28  blezek
 * Log: Refactored into Server.java, DartServer only processes command line arguments
 * Log:
 */
