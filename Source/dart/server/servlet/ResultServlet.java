package dart.server.servlet;

import java.io.File;
import java.io.*;
import java.util.Properties;
import java.util.zip.*;

import javax.servlet.http.*;
import javax.servlet.*;

import dart.server.*;
import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.xmlrpc.*;
import org.apache.log4j.Logger;

/**
 * A servlet to handle XMLRPC requests to the project
 * @author Dan Blezek
 * @version $Revision:$
 */
public class ResultServlet extends HttpServlet {
  static Logger logger = Logger.getLogger ( ResultServlet.class );   

  String servicename = null;
  Project project = null;
  XmlRpcServer xmlrpc = null;
  boolean deleteWhenDigested = false;
  /**
   * Empty constructor
   */
  public ResultServlet() {
    logger.info ( "Creating ResultServlet" );
  }
  /**
   * Handle Servlet requests
   */
  public void doGet (HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException  {
    logger.debug ( "Handling request" );
    byte[] result = xmlrpc.execute ( request.getInputStream() );
    response.setContentType ( "text/xml" );
    response.setContentLength ( result.length );
    response.getOutputStream().write ( result );
    response.getOutputStream().flush();
    logger.debug ( "Handled request: " + result );
  }

  /**
   * Set the name of the service
   * ServiceName is the "XML-RPC" object name.
   * @param name Name to set.
   */
  public void setServiceName ( String name ) {
    servicename = name;
  }

  /**
   * Start up the ResultServer
   * @param p Project to start this ResultServer for
   */
  public void start ( Project p ) throws Exception {
    project = p;
    xmlrpc = new XmlRpcServer();
    if ( servicename == null ) {
      servicename = p.getTitle();
    }
    xmlrpc.addHandler ( servicename, new ResultHandler() );
    // project.getXmlRpcServer().registerProxyService( new ResultHandler(), servicename );
  }

  /**
   * Delete XML files after digested into Dart
   * @param i Boolean string.
   */
  public void setDeleteWhenDigested ( String i ) {
    logger.debug ( "Setting DeleteWhenDigested to " + i );
    deleteWhenDigested = Boolean.valueOf ( i ).booleanValue();
    logger.debug ( "Set DeleteWhenDigested to " + deleteWhenDigested );
  }

  /**
   * Shutdown the ResultServer
   * At shutdown, the service is unregisterd with the XML-RPC server.
   */
  public void shutdown () throws Exception {
    // project.getXmlRpcServer().unregisterInvocationHandler ( servicename );
  }
   
  /**
   * Internal class to handle XML-RPC requests
   * @author Dan Blezek
   */
  public class ResultHandler {
    /**
     * Submit an array of binary data, assumed to be XML
     * This method detects if the data is uncompressed, and, if so
     * compress and write the data to the Projects Temporary Directory.
     * @param f Array of binary data to save
     */
    public void put ( byte [] f ) {
      File tmp = project.getUniqueFileInTemporaryDirectory ( ".xml.gz" );
      try {
        OutputStream out;
        int Magic = ((int)f[0] & 0xff) | ((f[1]<<8) & 0xff00 );
        out = new FileOutputStream ( tmp );
        if ( Magic != java.util.zip.GZIPInputStream.GZIP_MAGIC ) {
          // Data was not GZipped, GZip it on the way out
          logger.debug ( project.getTitle() + ": Data was not gzipped, gzipping during save" );
          out = new GZIPOutputStream ( out );
        }
        out.write ( f );
        out.flush();
        out.close();
      } catch ( Exception e ) {
        logger.error ( project.getTitle() + ": Failed to write", e );
      }

      // Add a job to the queue
      Properties prop = new Properties();
      try {
        prop.setProperty ( "URL", tmp.toURL().toString() );
        prop.setProperty ( "DeleteWhenDigested", Boolean.toString ( deleteWhenDigested ) );
      } catch ( Exception e ) {
        logger.error ( project.getTitle() + ": Failed to convert file to URL", e );
      }
      project.queueTask ( "dart.server.task.XMLResultProcessor", prop, 5 );
    }

    /**
     * Refresh the project resources
     * Handle a request to refresh the project resources
     * @see Project#refreshResources
     */
    public void refresh () {
      project.refreshResources();
    }
  }
    
}

/*
 * $Log:$
 */

