package qed;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.*;
import java.net.*;
import java.util.zip.GZIPOutputStream;

import org.apache.xmlrpc.XmlRpcClient;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.commons.vfs.*;

import org.apache.commons.cli.*;
public class QEDClient
{
  static Logger logger = Logger.getLogger ( QEDClient.class );   
  
  public static void main( String[] args )
  {
    BasicConfigurator.configure();
    logger.info ( "Starting QEDClient" );

    CommandLineParser parser = new PosixParser();
    Options options = new Options();
    CommandLine cmd = null;
    int port = 8081;
    boolean printHelp = false;
    options.addOption ( "p", "port", true, "XML-RPC Port to connect to, 8081 is default" );
    options.addOption ( "s", "server", true, "Server to connect to, localhost is default" );
    options.addOption ( "h", "help", false, "Print help message" );
    options.addOption ( "o", "population", true, "Return the a PopulationId" );
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
      new HelpFormatter().printHelp ( "QEDClient [options] <QED Project>", options );
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
      client = new XmlRpcClient ( new URL ( "http://" + cmd.getOptionValue ( "s", "localhost" ) + ":" + port + "/" + project + "/Command/" ) );
    } catch ( Exception e ) {
      logger.error ( "Failed to create XmlRpcClient", e );
      System.exit ( 1 );
    }

    if ( cmd.hasOption ( "o" ) ) {
      String name = cmd.getOptionValue ( "o" );
      try {
        Vector arguments = new Vector();
        arguments.addElement ( cmd.getOptionValue ( "o" ) );
        Integer populationId = (Integer)client.execute ( "Write.getPopulation", arguments );
        logger.info ( "Got: " + populationId );
      } catch ( Exception e ) {
        logger.error ( "Failed to create or get population " + name, e );
        System.exit ( 1 );
      }
    }
  }
}
