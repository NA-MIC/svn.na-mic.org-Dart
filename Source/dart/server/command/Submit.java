package dart.server.command;

import dart.server.*;
import java.io.File;
import java.io.*;
import java.util.Properties;
import java.util.zip.*;

import org.apache.log4j.Logger;

/*
 * Class to handle submission commands
 * @author Dan Blezek
 */
public class Submit implements Command {
  Project project = null;
  static Logger logger = Logger.getLogger ( Submit.class );   
  Properties properties;

  public Submit ( Container p, Properties prop ) throws Exception {
    if ( p instanceof Project ) {
      project = (Project)p;
      properties = prop;
      logger.debug ( project.getTitle() + ": Created Submit Command" );
    } else {
      throw new Exception ( "Submit can only work on Projects" );
    }
  }
  /**
   * Submit an array of binary data, assumed to be XML
   * This method detects if the data is uncompressed, and, if so
   * compress and write the data to the Projects Temporary Directory.
   * @param f Array of binary data to save
   */
  public boolean put ( byte [] f ) {
    File tmp = project.getUniqueFileInTemporaryDirectory ( ".xml.gz" );
    if ( f.length < 2 ) {
      logger.info ( project.getTitle() + ": data length is less than 2 bytes, rejecting." );
      return false;
    }
    OutputStream out = null;
    try {
      int Magic = ((int)f[0] & 0xff) | ((f[1]<<8) & 0xff00 );
      out = new FileOutputStream ( tmp );
      if ( Magic != java.util.zip.GZIPInputStream.GZIP_MAGIC ) {
        // Data was not GZipped, GZip it on the way out
        logger.debug ( project.getTitle() + ": Data was not gzipped, gzipping during save" );
        out = new GZIPOutputStream ( out );
      }
      out.write ( f );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Failed to write", e );
      return false;
    } finally {
      if ( out != null ) {
        try {
          out.flush();
          out.close();
        } catch ( IOException ee ) {}
      }
    }

    // Add a job to the queue
    Properties prop = new Properties();
    boolean deleteWhenDigested = true;
    try {
      prop.setProperty ( "URL", tmp.toURL().toString() );
      prop.setProperty ( "DeleteWhenDigested", Boolean.toString ( deleteWhenDigested ) );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Failed to convert file to URL", e );
    }
    project.queueTask ( "dart.server.task.XMLResultProcessor", prop, 5 );
    return true;
  }

  /**
   * Refresh the project resources
   * Handle a request to refresh the project resources
   * @see Project#refreshResources
   */
  public boolean refresh () {
    project.refreshResources();
    return true;
  }
}
