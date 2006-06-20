package dart.server;

import java.io.*;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;

import org.apache.log4j.Logger;

import java.sql.Connection;
import net.sourceforge.jaxor.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.*;
import freemarker.ext.beans.*;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.NotFoundHandler;
import org.mortbay.http.HttpException;

import dart.server.wrap.*;


/**
 * Handler usually attached to the root Dart server httpContext such
 * that unknown requests yield a list of active projects
 */
public class DefaultNotFoundHandler extends NotFoundHandler
{
  static Logger logger = Logger.getLogger ( Server.class );   
  Server dartServer = null;
  
  public DefaultNotFoundHandler() {}

  /**
   * Set the Dart server this handler will interface with.
   */
  public void setDartServer(Server s) { dartServer = s; }

  /**
   * Get the Dart server.
   */
  public Server getDartServer() { return dartServer; }

  /**
   * Handle a not found event
   */
  public void handle( String pathInContext, String pathParams, HttpRequest req, HttpResponse res) throws HttpException, java.io.IOException
  {
    // setup the output
    res.setContentType( "text/html" );
    PrintWriter out = new PrintWriter(res.getOutputStream());

    // Cache information for the template engine
    HashMap root = new HashMap();
    root.put( "date", Calendar.getInstance().getTime() );
    root.put( "projects", dartServer.projects );
    root.put( "request", req );

    // Run the template to generate the page
    Configuration cfg = new Configuration();
    File resourcesDirectory = new File(dartServer.getBaseDirectory(),
                                       "Templates");
    cfg.setDirectoryForTemplateLoading( resourcesDirectory );

    Template template = cfg.getTemplate ( "DefaultNotFound.ftl" );

    try {
      template.process( root, out );
    } catch ( Exception e ) {
    } finally {
      out.close();
    }
    
  }
  
}

