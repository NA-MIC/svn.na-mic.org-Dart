package dart.server.servlet;

import java.io.*;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.security.Principal;

import javax.servlet.http.*;
import javax.servlet.*;

import java.sql.Connection;
import net.sourceforge.jaxor.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.*;
import freemarker.ext.beans.*;
import freemarker.ext.servlet.HttpSessionHashModel;

import org.apache.log4j.Logger;

import org.mortbay.http.UserRealm;
import org.mortbay.http.HashUserRealm;

import dart.server.Server;
import dart.server.Project;
import dart.server.track.Track;
import dart.server.wrap.*;


/**
   Servlet to prepare data for template processing.
   The Dashboard servlet prepares a set of submissions for template
   processing by FreeMarker.  It's behavior is dictated by the
   parameters, and is implemented as a giant switch statement.  The
   parameter options are:
   <ul>
     <li>Default: Find all submissions on tracks that intersect "now"</li>
     <li><code>trackid</code> find all submission that intersect the EndTime of the given track</li>
     <li><code>submissionid</code> find a single submission.  Passed
     to the template as <code>submission</code></li>
     <li><code>order</code> order by these columns, may be multiple</li>
   </ul>
*/


public class Dashboard extends HttpServlet {
  // May not have an ivars, as the servlet engines are threaded

  // might not want to have a logger in the servlet
  static Logger logger = Logger.getLogger ( Dashboard.class );

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
    //
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
      RequestDispatcher dispatcher = req.getRequestDispatcher( "Dashboard" );
      dispatcher.forward( req, res );
      return;
    }

    // since servlets cannot have ivars, use a local variable (map) to
    // store information to pass to other methods of the servlet
    //
    HashMap map = new HashMap();
    map.put ( "request", req );
    map.put ( "response", res );

    // setup the output.  if the templateName ends in .xml (so the
    // full template name is *.xml.ftl), then configure the output to
    // be xml.  Otherwise, configure the output for html.
    if (templateName.endsWith(".xml")) {
      res.setContentType( "text/xml");
    } else {
      res.setContentType( "text/html" );
    }
    PrintWriter out = res.getWriter();
    map.put ( "out", out );

    // get the project name
    //
    String projectName = getInitParameter("project");
    map.put ( "projectName", projectName );

    Project project;
    try {
      project = Server.getProject( projectName );
      if (project == null) {
        logger.debug( project.getTitle() + ": not found" );
        error ( out, "Dart Dashboard", "Dart: no project found matching \"" + projectName + "\"", map );
        out.close();
        return;
        }
    } catch ( Exception e ) {
      logger.debug( projectName + ": error getting project" );
      error ( out, "Dart Dashboard", "Dart: Error accessing project \"" + projectName + "\"", map );
      out.close();
      return;
    }
    map.put ( "project", project );

    // connect to the project database
    //
    Connection connection = project.getConnection();
    try {
      connection.setReadOnly ( true );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Could not set connection to ReadOnly, possible security hole!", e );
    }
    JaxorContextImpl jaxorContext = new JaxorContextImpl ( connection );
    SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( jaxorContext );
    TrackFinderBase trackFinder = new TrackFinderBase ( jaxorContext );
    ClientFinderBase clientFinder = new ClientFinderBase ( jaxorContext );

    // Setup the map of information that will be passed to the
    // template engine
    //
    HashMap root = new HashMap();
    root.put ( "serverName", project.getServer().getTitle() );
    root.put ( "projectName", projectName );
    root.put ( "fetchdata", new FetchData ( project ) );
    root.put ( "projectProperties", project.getProperties() );
    root.put ( "request", req );
    root.put ( "submissionFinder", submissionFinder );

    BeansWrapper realmWrapper = new BeansWrapper();
    realmWrapper.setExposureLevel( BeansWrapper.EXPOSE_ALL );
    try {
      root.put ( "realm", realmWrapper.wrap(project.getHttpServer().getRealm("Dart")));
    } catch (Exception e) {}
    
    // Put in the request parameters
    Map parameters = req.getParameterMap();
    root.put ( "parameters", parameters );

    BeansWrapper wrapper = new BeansWrapper();
    wrapper.setExposureLevel ( BeansWrapper.EXPOSE_ALL );
    try {
      root.put ( "writableParameters", wrapper.wrap ( new HashMap ( parameters ) ) );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Could not wrap map", e );
      error ( out, "Dart Dashboard", "Dart: Failed to wrap parameters", map );
      return;
    }
      
    // put the http session in the data model for the template
    // engine. the session is wrapped so that it appears as a
    // hash. attributes of the session can be accessed as
    // ${session.foo} in the template.
    HttpSession httpSession = req.getSession(false);
    if (httpSession != null) {
      HttpSessionHashModel httpSessionModel
        = new HttpSessionHashModel(httpSession, ObjectWrapper.DEFAULT_WRAPPER);
      root.put("session", httpSessionModel);
    } else {
      // logger.info("Dashboard: no session");
    }
      
    // Put the intersecting tracks on the submission
    // 
    java.util.Date date;
    if ( parameters.containsKey ( "trackid" ) ) {
      // trackid specified
      String key = ((String[])parameters.get ( "trackid" ))[0];
      TrackEntity te = trackFinder.selectByPrimaryKey ( new Long ( key ) );
      date = new java.util.Date ( te.getEndTime().getTime() - 1000 );
    } else if (parameters.containsKey( "timestamp" ) ) {
      // datestamp specified
      String stamp = ((String[])parameters.get ( "timestamp" ))[0];
      date =  project.parseBuildStamp( stamp );
    } else if (parameters.containsKey("submissionid") ) {
      String key = ((String[])parameters.get ( "submissionid" ))[0];
      SubmissionEntity se = submissionFinder.selectByPrimaryKey ( new Long ( key ) );
      date = new java.util.Date ( se.getTimeStamp().getTime() );
    } else {
      // otherwise, use "now"
      date = new java.util.Date();
    }
    // Force the date to be no later than "now"
    java.util.Date now = new java.util.Date();
    if (date.after( now ) ) {
      date = now;
    }
    String ts = new SimpleDateFormat( "yyyyMMdd" ).format ( date );
    root.put ( "date", date );
    root.put ( "timestamp", ts );
    java.sql.Timestamp timeStamp = new java.sql.Timestamp ( date.getTime() );
    
    // Make sure they all exist
    project.getTrackManager().getTrackIds ( timeStamp );
    
    // Find intersecting tracks
    TrackList trackEntityList = trackFinder.selectIntersectingList( timeStamp, timeStamp );
    root.put ( "tracks", getTrackMap ( trackEntityList) );
    root.put ( "defaulttrack", project.getTrackManager().getDefaultTrack() );
    root.put( "trackorder", project.getTrackManager().getTrackOrder() );
    
    // find submissions that matches the query
    //
    if ( parameters.containsKey ( "submissionid" ) ) {
      // Search by submission id specified on the url
      String[] ids = (String[])parameters.get ( "submissionid" );
      SubmissionEntity submission
        = submissionFinder.selectBySubmissionId ( new Long ( ids[0] ) );
      root.put ( "submission", submission );
    } else if ( parameters.containsKey( "site" )
                && parameters.containsKey( "buildname" )
                && parameters.containsKey( "track" ) ) {
      // Search for submission id by client name, track and date
      //

      // first find the client
      QueryParams qc = new QueryParams();
      qc.add( new net.sourceforge.jaxor.mappers.StringMapper(),
              ((String[])parameters.get("site"))[0]);
      qc.add( new net.sourceforge.jaxor.mappers.StringMapper(),
              ((String[])parameters.get("buildname"))[0]);
      ClientList clist  
        = clientFinder.find( "where site = ? and buildname = ?", qc);

      if (clist.size() > 0) {
        ClientEntity c = (ClientEntity) clist.toList().get(0);
        
        // find the submission
        QueryParams qs = new QueryParams();
        qs.add( new net.sourceforge.jaxor.mappers.StringMapper(), timeStamp);
        qs.add( new net.sourceforge.jaxor.mappers.LongMapper(),
                c.getClientId());
        qs.add( new net.sourceforge.jaxor.mappers.StringMapper(),
                ((String[])parameters.get("track"))[0]);
        
        SubmissionList slist
          = submissionFinder.find("where timestamp = ? and clientid = ? and type = ?", qs);

        if (slist.size() > 0) {
          root.put ( "submission", (SubmissionEntity)slist.toList().get(0) );
        }
      }
    }

    // find the client the matches the query
    //
    if ( parameters.containsKey( "clientid" )) {
      // Search for the client id specified on the url
      String[] ids = (String[])parameters.get ( "clientid" );
      ClientEntity client = null;
      try {
        client = clientFinder.selectByClientId ( new Long ( ids[0] ) );
        root.put ( "client", client );
      } catch (Exception e) {logger.error("Client not found. id = " + ids[0]);}
    }
      
    // find the template specified on the URL
    //
    try {
      findTemplate ( map );
    } catch ( Exception e ) {
      logger.error ( "Failed to find template", e );
      error ( out, "Dart Dashboard", "Dart: Failed to find or parse template: \"" + (String) map.get ( "templateName" ) + "\"", map );
      try { connection.close(); } catch ( Exception e2 ) { }
      out.close();
      return;
    }

    // finally run the template to generate the webpage
    //
    try {
      Template template = (Template) map.get ( "template" );
      template.process ( root, out );
    } catch ( Exception e ) {
    } finally {
      try { connection.close(); } catch ( Exception e ) { }
    }
    out.close();
  }

  HashMap getTrackMap ( TrackList trackList ) {
    HashMap trackMap = new HashMap();
    TrackEntity[] trackArray = trackList.toArray();
    for ( int i = 0; i < trackArray.length; i++ ) {
      trackMap.put ( trackArray[i].getName(), trackArray[i] );
    }
    return trackMap;
  }

  // locate the template specified on the URL pathinfo.  The parameter
  // "map" is used to pass information into and out of the method
  // (this servlet cannot have ivars since multiple threads may be
  // making different requests).
  // 
  void findTemplate ( HashMap map ) throws Exception {
    HttpServletRequest request = (HttpServletRequest) map.get ( "request" );
    Project project = (Project) map.get ( "project" );

    // Find the template name from the URL pathinfo
    String templateName = request.getPathInfo();
    map.put ( "templateName", templateName );

    // default mechanism for loading templates is to look in the
    // project's "Templates" directory. it is possible to load
    // templates from multiple locations.  See the example in
    // Extras/Plugins/ProjectStatistics/ProjectStatistics.java 
    Configuration cfg = new Configuration();
    File resourcesDirectory = new File(project.getBaseDirectory(),"Templates");
    cfg.setDirectoryForTemplateLoading( resourcesDirectory );

    Template template = cfg.getTemplate ( templateName + ".ftl" );
    map.put ( "template", template );
  }
}
