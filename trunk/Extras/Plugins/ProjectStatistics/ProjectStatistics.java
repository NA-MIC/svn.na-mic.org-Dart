package dart.extras.servlet;

import java.io.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.http.*;
import javax.servlet.*;

import java.sql.Connection;
import net.sourceforge.jaxor.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.*;
import freemarker.ext.beans.*;
import freemarker.cache.*;

import org.apache.log4j.Logger;

import dart.DartServer;
import dart.server.Project;
import dart.server.wrap.*;


/**
   Example Servlet to display Project statistics.

   The resulting class file and corresponding template file can be
   placed in a project's Plugins directory or rolled into a jar and
   placed in a projects Plugins directory.

*/


public class ProjectStatistics extends HttpServlet {
  // May not have an ivars, as the servlet engines are threaded

  // might not want to have a logger in the servlet
  static Logger logger = Logger.getLogger ( ProjectStatistics.class );

  void error ( PrintWriter out, String title, String msg, HashMap map ) {
    String projectName = (String) map.get ( "projectName" );
    out.println( "<html><head><title>" + title + "</title></head>" );
    out.println( "<body>" );
    out.println( "<h1>Project statistics for " );
    out.println( projectName );
    out.println( "</h1>" );
    out.println( "<p>" );
    out.println( msg );
    out.println( "<p>" );
    out.println( "</body>" );
    out.println( "</html>" );
    return;
  }    

  public void doGet (HttpServletRequest req,
                     HttpServletResponse res)
    throws ServletException, IOException
  {
    // since servlets cannot have ivars, use a local variable (map) to
    // store information to pass to other methods of the servlet
    HashMap map = new HashMap();
    map.put ( "request", req );
    map.put ( "response", res );

    // setup the output
    res.setContentType( "text/html" );
    PrintWriter out = res.getWriter();
    map.put ( "out", out );

    // first get the project
    String projectName = getInitParameter("project");
    map.put ( "projectName", projectName );

    Project project;
    try {
      project = DartServer.getProject( projectName );
      if (project == null) {
        logger.debug( project.getTitle() + ": not found" );
        error ( out, "Dart Project Statistics", "Dart: no project found matching \"" + projectName + "\"", map );
        out.close();
        return;
        }
    } catch ( Exception e ) {
      logger.debug( projectName + ": error getting project" );
      error ( out, "Dart Project Statistics", "Dart: Error accessing project \"" + projectName + "\"", map );
      out.close();
      return;
    }
    map.put ( "project", project );

    
    // setup the template loading mechanism.  We'll use the default
    // mechanism of loading templates from the project's template
    // directory, plus we'll load templates from wherever this class
    // file was found.  The former allows for default project
    // templates to be found. The latter allows for a plugin specific
    // template to be found.
    FileTemplateLoader ftl = new FileTemplateLoader( new File(project.getBaseDirectory(),"Templates") );
    ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "");
    TemplateLoader[] loaders = new TemplateLoader[] { ctl, ftl };
    MultiTemplateLoader mtl = new MultiTemplateLoader( loaders );

    Configuration cfg = new Configuration();
    cfg.setTemplateLoader( mtl );
    
    // generate a webpage using template engine
    HashMap root = new HashMap();
    root.put ( "projectName", projectName );

    try {
      root.put ( "projectStats", project.getStats() );
    } catch (Exception e) {
    }
    
    java.util.Date date = new java.util.Date();
    String ts = new SimpleDateFormat( "yyyyMMdd" ).format( date );
    root.put( "date", date );
    
    // Put in the request parameters
    Map parameters = req.getParameterMap();
    root.put ( "parameters", parameters );
    
    BeansWrapper wrapper = new BeansWrapper();
    wrapper.setExposureLevel ( BeansWrapper.EXPOSE_ALL );
    try {
      root.put ( "writableParameters", wrapper.wrap ( new HashMap ( parameters ) ) );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Could not wrap map", e );
      error ( out, "Dart Project Statistics", "Dart: Failed to wrap parameters", map );
      return;
    }
      
    // connect to the database
    Connection connection = project.getConnection();
    try {
      connection.setReadOnly ( true );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Could not set connection to ReadOnly, possible security hole!", e );
    }
    JaxorContextImpl jaxorContext = new JaxorContextImpl ( connection );

    try {
      Template template = cfg.getTemplate("ProjectStatistics.ftl");
      template.process ( root, out );
    } catch ( Exception e ) {
    } finally {
      try { connection.close(); } catch ( Exception e ) { }
    }
    out.close();
  }
}
