package qed.server.servlet;

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

import org.apache.log4j.Logger;

import qed.server.*;
import dart.server.*;
import dart.server.servlet.*;
import qed.server.wrap.*;


/**
   Servlet to prepare data for template processing.
   The View servlet prepares a set of submissions for template
   processing by FreeMarker.  It's behavior is dictated by the
   parameters, and is implemented as a giant switch statement.  The
   parameter options are:
   <ul>
     <li>Default: Find all Populations</li>
     <li><code>populationid</code> single population</li>
   </ul>
*/


public class View extends HttpServlet {
  // May not have an ivars, as the servlet engines are threaded

  // might not want to have a logger in the servlet
  static Logger logger = Logger.getLogger ( View.class );

  void error ( PrintWriter out, String title, String msg, HashMap map ) {
    String projectName = (String) map.get ( "projectName" );
    out.println( "<html><head><title>" + title + "</title></head>" );
    out.println( "<body>" );
    out.println( "<h1>Dashboard for " );
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
    // check the template name encoded in pathinfo and forward as necessary
    String templateName = req.getPathInfo();
    if ( templateName == null ) {
      // URL was of the form http://.../Project/Dashboard
      //
      // pathinfo empty, no template specified, use a "redirect"
      // mechanism to a URL with a trailing "/".  This "redirect" will
      // then be "forwarded" by the "else if" clause (below) to the
      // default Dashboard template.
      //
      //logger.info( "No pathinfo");
      res.sendRedirect( req.getRequestURL() + "/" );
      return;
    } else if ( templateName.equals ( "/" ) ) {
      // URL was http://.../Project/Dashboard/
      // 
      // pathinfo is the trailing "/", no template specified, use a
      // "forward" mechanism to indicate the default Dashboard
      // template should be used.
      // 
      //logger.info( "No template specified, using Dashboard template");
      RequestDispatcher dispatcher = req.getRequestDispatcher( "View" );
      dispatcher.forward( req, res );
      return;
    }

    // since servlets cannot have ivars, use a local variable (map) to
    // store information to pass to other methods of the servlet
    HashMap map = new HashMap();
    map.put ( "request", req );
    map.put ( "response", res );

    // setup the output
    res.setContentType( "text/html" );
    PrintWriter out = res.getWriter();
    map.put ( "out", out );

    /*
    out.println ( "<html>"
                  + "<head>"
                  + "<title>View</title>"
                  + "</head>"
                  + "<body>Foo bar</body>"
                  + "</html>" );
    */
    // get the project name
    String qedName = getInitParameter("qed");
    map.put ( "name", qedName );

    QED qed;
    try {
      /*
      qed = Server.getQED( qedName );
      if (qed == null) {
        logger.debug( qed.getTitle() + ": not found" );
        error ( out, "Dart Dashboard", "Dart: no qed found matching \"" + qedName + "\"", map );
        out.close();
        return;
        }
        */
    } catch ( Exception e ) {
      logger.debug( qedName + ": error getting qed" );
      error ( out, "Dart Dashboard", "Dart: Error accessing qed \"" + qedName + "\"", map );
      out.close();
      return;
    }
    // map.put ( "qed", qed );

    // find the template specified on the URL
    try {
      findTemplate ( map );
    } catch ( Exception e ) {
      logger.error ( "Failed to find template", e );
      error ( out, "Dart Dashboard", "Dart: Failed to find or parse template: \"" + (String) map.get ( "templateName" ) + "\"", map );
      out.close();
      return;
    }

    // Setup the map of information that will be passed to the
    // template engine
    HashMap root = new HashMap();
    root.put ( "qedName", qedName );
    // root.put ( "fetchdata", new FetchData ( qed ) );
    // root.put ( "qedProperties", qed.getProperties() );

    // Put in the request parameters
    Map parameters = req.getParameterMap();
    root.put ( "parameters", parameters );

    // connect to the database
    /*
    Connection connection = qed.getConnection();
    try {
      connection.setReadOnly ( true );
    } catch ( Exception e ) {
      logger.error ( qed.getTitle() + ": Could not set connection to ReadOnly, possible security hole!", e );
    }
    
    JaxorContextImpl jaxorContext = new JaxorContextImpl ( connection );
    PopulationFinderBase populationFinder = new PopulationFinderBase ( jaxorContext );
    SubjectFinderBase subjectFinder = new SubjectFinderBase ( jaxorContext );
    ExperimentFinderBase experimentFinder = new ExperimentFinderBase ( jaxorContext );
    RunFinderBase runFinder = new RunFinderBase ( jaxorContext );

    if ( parameters.containsKey ( "populationid" ) ) {
      String[] ids = (String[])parameters.get ( "populationid" );
      if ( ids[0].equals ( "all" ) ) {
        PopulationList list = populationFinder.query ( "select * from population" );
        root.put ( "populations", list );
      } else {
        PopulationEntity population = populationFinder.selectByPopulationId ( new Integer ( ids[0] ) );
        root.put ( "population", population );
      }
    } else {
      PopulationList list = populationFinder.query ( "select * from population" );
      root.put ( "populations", list );
    }

    // finally run the template to generate the webpage
    try {
      Template template = (Template) map.get ( "template" );
      template.process ( root, out );
    } catch ( Exception e ) {
    } finally {
      try { connection.close(); } catch ( Exception e ) { }
    }
    out.close();
  }


  // locate the template specified on the URL pathinfo.  The parameter
  // "map" is used to pass information into and out of the method
  // (this servlet cannot have ivars since multiple threads may be
  // making different requests).
  // 
  void findTemplate ( HashMap map ) throws Exception {
    HttpServletRequest request = (HttpServletRequest) map.get ( "request" );
    QED qed = (QED) map.get ( "qed" );

    // Find the template name from the URL pathinfo
    String templateName = request.getPathInfo();
    map.put ( "templateName", templateName );

    // default mechanism for loading templates is to look in the
    // qed's "Templates" directory. it is possible to load
    // templates from multiple locations.  See the example in
    // Extras/Plugins/QEDStatistics/QEDStatistics.java 
    Configuration cfg = new Configuration();
    File resourcesDirectory = new File(qed.getBaseDirectory(),"Templates");
    cfg.setDirectoryForTemplateLoading( resourcesDirectory );

    Template template = cfg.getTemplate ( templateName + ".ftl" );
    map.put ( "template", template );
  }
  */
}
/* $Log$ */
