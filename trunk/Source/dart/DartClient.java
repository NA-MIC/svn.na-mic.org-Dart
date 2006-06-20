package dart;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.*;
import java.net.*;
import java.util.zip.GZIPOutputStream;

import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.*;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.commons.vfs.*;

import org.apache.commons.cli.*;
public class DartClient
{
  static Logger logger = Logger.getLogger ( DartClient.class );   
  
  public static void main( String[] args )
  {
    BasicConfigurator.configure();
    logger.info ( "Starting DartClient" );

    CommandLineParser parser = new PosixParser();
    Options options = new Options();
    CommandLine cmd = null;
    int port = 8081;
    boolean printHelp = false;
    options.addOption ( "p", "port", true, "XML-RPC Port to connect to, 8081 is default" );
    options.addOption ( "s", "server", true, "Server to connect to, localhost is default" );
    options.addOption ( "h", "help", false, "Print help message" );
    options.addOption ( "q", "shutdown", false, "Shutdown the Server" );
    options.addOption ( "r", "refresh", false, "Refresh Project resources" );
    options.addOption ( "R", "refreshServer", false, "Refresh Server resources" );
    options.addOption ( "g", "getstatus", false, "Get Server status" );
    options.addOption ( "z", "schedulerstatus", false, "Get Scheduler status" );
    options.addOption ( "d", "date", false, "Print the current date and exit" );
    options.addOption ( "w", "password", true, "ProjectAdministrator password" );
    options.addOption ( "u", "username", true, "ProjectAdministrator username" );
    options.addOption ( "l", "sql", true, "SQL Commands to run on server" );
    try {
      cmd = parser.parse ( options, args );
    } catch ( Exception e ) {
      logger.error ( "Failed to parse commandline", e );
      printHelp = true;
    }

    String[] files = cmd.getArgs();
    if ( cmd.hasOption ( "h" ) 
         || printHelp 
         || files.length < 1 ) {
      new HelpFormatter().printHelp ( "DartClient [options] Project <foo1.xml> <foo2.xml> ... <fooN.xml>\n"
                                      + "\tTo use a proxy, specify -DproxyHost=host -DproxyPort=8080 on the commandline", options );
      System.exit ( 0 );
    }

    try { 
      port = Integer.parseInt ( cmd.getOptionValue ( "p", "8081" ) );
    } catch ( Exception e ) {
      logger.error ( cmd.getOptionValue ( "p", "8081" ) + " is not a valid port number" );
      System.exit ( 1 );
    }

    String project = files[0]; 


    XmlRpcClient client = null;
    try {
      XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
      config.setServerURL(new URL ( "http://" + cmd.getOptionValue ( "s", "localhost" ) + ":" + port + "/" + project + "/Command/" ) );
      client = new XmlRpcClient ();
      client.setConfig(config);
    } catch ( Exception e ) {
      logger.error ( "Failed to create XmlRpcClient", e );
      System.exit ( 1 );
    }

    if ( cmd.hasOption ( "q" ) ) {
      try {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL ( new URL ( "http://" + cmd.getOptionValue ( "s", "localhost" ) + ":" + port + "/DartServer/Command/" ) );
        XmlRpcClient admin = new XmlRpcClient ( );
        admin.setConfig ( config );
        Vector aa = new Vector ();
        aa.addElement ( "Foo" );
        aa.addElement ( "bar" );
        admin.execute ( "Administration.shutdown", aa );
      } catch ( Exception e ) {
        logger.error ( "Failed to shutdown project", e );
        System.exit ( 1 );
      }
    }

    if ( cmd.hasOption ( "r" ) ) {
      try {
        client.execute ( "Submit.refresh", new Vector() );
        System.exit ( 0 );
      } catch ( Exception e ) {
        logger.error ( "Failed to refresh project", e );
        System.exit ( 1 );
      }
    }

    if ( cmd.hasOption ( "R" ) ) {
      try {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL ( new URL ( "http://" + cmd.getOptionValue ( "s", "localhost" ) + ":" + port + "/DartServer/Command/" ) );
        XmlRpcClient admin = new XmlRpcClient ( );
        admin.setConfig ( config );
        admin.execute ( "Administration.refresh", new Vector() );
        System.exit ( 0 );
      } catch ( Exception e ) {
        logger.error ( "Failed to refresh server", e );
        System.exit ( 1 );
      }
    }

    if ( cmd.hasOption ( "l" ) ) {
      // Execute sql commands
        Vector params = new Vector();
        params.addElement ( cmd.getOptionValue ( "u", "" ) );
        params.addElement ( cmd.getOptionValue ( "w", "" ) );
        
        // Read the SQL file
        File SQL = new File ( cmd.getOptionValue ( "l", "" ) );
        if ( !SQL.exists() ) { 
          logger.error ( "File: " + SQL.toString() + " does not exist" );
          System.exit ( 1 );
        }
        try {
          Reader in = new BufferedReader ( new FileReader ( SQL ) );
          StringWriter out = new StringWriter ();
          char[] Data = new char[3000];
          while ( true ) {
            int bytesRead = 0;
            bytesRead = in.read ( Data );
            if ( bytesRead == -1 ) {
              break;
            }
            out.write ( Data, 0, bytesRead );
          }
          params.addElement ( out.toString() );
          client.execute ( "ProjectAdministration.runSQL", params );
          System.exit ( 0 );
        } catch ( Exception adminEx ) {
          logger.error ( "Failed to execute runSQL command", adminEx );
          System.exit ( 1 );
        }
    }
    
    
    if ( cmd.hasOption ( "g" ) ) {
      try {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL ( new URL ( "http://" + cmd.getOptionValue ( "s", "localhost" ) + ":" + port + "/DartServer/Command/" ) );
        XmlRpcClient admin = new XmlRpcClient ( );
        admin.setConfig ( config );
        String o = (String)admin.execute ( "Administration.getStatus", new Vector() );
        // Vector params = new Vector();
        // String result = (String) client.execute ( "
        logger.info ( "Status: " + o );
        System.exit ( 0 );
      } catch ( Exception e ) {
        logger.error ( "Failed to get project status", e );
        System.exit ( 1 );
      }
    }

    if ( cmd.hasOption ( "z" ) ) {
      try {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL ( new URL ( "http://" + cmd.getOptionValue ( "s", "localhost" ) + ":" + port + "/DartServer/Command/" ) );
        XmlRpcClient admin = new XmlRpcClient ( );
        admin.setConfig ( config );
        String o = (String)admin.execute ( "Administration.getSchedulerStatus", new Vector() );
        // Vector params = new Vector();
        // String result = (String) client.execute ( "
        logger.info ( "Status: " + o );
        System.exit ( 0 );
      } catch ( Exception e ) {
        logger.error ( "Failed to get project status", e );
        System.exit ( 1 );
      }
    }

    for ( int i = 1; i < files.length; i++ ) {
      try {
        byte[] Data = new byte[32000];
        File inputfile = new File ( files[i] );
        logger.info ( inputfile + " Length: " + inputfile.length() );
        if ( inputfile.length() <= 4  ) { logger.info ( inputfile.toString() + " short file, skipping" ); continue; }
        // Reject anything more than 20 m
        if ( inputfile.length() > 1024 * 1024 * 10 ) {
          long l = (long) ( inputfile.length() / (1024.*1024.) );
          logger.warn ( "Skipping " + inputfile.toString() + " Length: " + l + "M" );
          continue;
        }
        InputStream in = new BufferedInputStream ( new FileInputStream ( inputfile ) );
        ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        OutputStream out = null;
        if ( inputfile.getName().endsWith ( ".gz" ) ) {
          out = new BufferedOutputStream ( bytes );
        } else {
          out = new GZIPOutputStream ( bytes );
        }
        while ( true ) {
          int bytesRead = 0;
          bytesRead = in.read ( Data );
          if ( bytesRead == -1 ) {
            break;
          }
          out.write ( Data, 0, bytesRead );
        }
        if ( out instanceof GZIPOutputStream ) {
          ((GZIPOutputStream)out).finish();
        }
        out.flush();
        if ( bytes.toByteArray().length == 0 ) { 
          logger.error ( "bytes is zero length" );
        }

        Vector params = new Vector();
        params.addElement ( bytes.toByteArray() );
        client.execute ( "Submit.put", params );
        // client.invoke ( project + ".put", new Object[] { bytes.toByteArray() } );
        in.close();
      } catch ( Exception e ) {
        logger.error ( "Failed to call Dart with file: " + files[i], e );
        System.exit ( 1 );
      }
    }
    System.exit ( 0 );
  }
}
