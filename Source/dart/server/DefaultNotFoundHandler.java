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
    
    // for each project on the server, determine the last time results
    // were added to the project's database.
    HashMap activity = new HashMap();

    Iterator it = dartServer.projects.entrySet().iterator();
    while ( it.hasNext() ) {
      // get the project
      Map.Entry entry = (Map.Entry) it.next();
      Project project = (Project) entry.getValue();

      // find the last task in the completed task table for this
      // project. restrict the query to just those tasks that are of
      // type XMLResultProcessor, i.e. a task that entered results
      // into the database. This ignores any other tasks that were run
      // on the project such as archival, statistics, etc.
      Connection connection = project.getConnection();
      try {
        connection.setReadOnly ( true );
      } catch ( Exception e ) {
        logger.error ( project.getTitle()
           + ": Could not set connection to ReadOnly, possible security hole!",
           e );
      }
      
      JaxorContextImpl jaxorContext = new JaxorContextImpl ( connection );
      CompletedTaskFinderBase taskFinder
        = new CompletedTaskFinderBase( jaxorContext );

      QueryParams params = new QueryParams();
      params.add( "dart.server.task.XMLResultProcessor" );

      CompletedTaskList taskList = taskFinder.query("select * from CompletedTask where Type=? order by ProcessedTime desc", params );

      // logger.info("Completed task length: " + taskList.size() );

      CompletedTaskIterator lit = taskList.iterator();
      if (lit.hasNext()) {
        java.sql.Timestamp timestamp
          = (java.sql.Timestamp) lit.next().getProcessedTime();

        // cache the last timestamp
        activity.put( project.getTitle(), timestamp );
      } else {
        java.sql.Timestamp timestamp = new java.sql.Timestamp(0);

        // cache the last timestamp
        activity.put( project.getTitle(), timestamp );
      }
      
      // close the connection to this project
      try {
        connection.close();
      } catch (Exception e) {
        logger.error("Unable to close connection to database.");
      }
    }
    // pass the last activity times freemarker
    root.put( "activity", activity );
    

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

