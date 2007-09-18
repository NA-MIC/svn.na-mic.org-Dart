package dart.server.servlet;

import java.io.*;

import java.util.HashMap;
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
import dart.server.wrap.*;


/**
   Servlet to prepare data for template processing.
   The Admin servlet contains the application logic for the
   administering a Dart project.
*/


public class Admin extends HttpServlet {
  // May not have an ivars, as the servlet engines are threaded

  // might not want to have a logger in the servlet
  static Logger logger = Logger.getLogger ( Admin.class );

  void error ( PrintWriter out, String title, String msg, HashMap map ) {
    String projectName = (String) map.get ( "projectName" );
    out.println( "<html><head><title>" + title + "</title></head>" );
    out.println( "<body>" );
    out.println( "<h1>Administration for " );
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
      // URL was of the form http://.../Project/Admin
      //
      // pathinfo empty, no template specified, use a "redirect"
      // mechanism to a URL with a trailing "/".  This "redirect" will
      // then be "forwarded" by the "else if" clause (below) to the
      // default Admin template.
      //
      //logger.info( "No pathinfo");
      res.sendRedirect( req.getRequestURL() + "/" );
      return;
    } else if ( templateName.equals ( "/" ) ) {
      // URL was http://.../Project/Admin/
      // 
      // pathinfo is the trailing "/", no template specified, use a
      // "forward" mechanism to indicate the default Admin
      // template should be used.
      // 
      //logger.info( "No template specified, using Admin template");
      RequestDispatcher dispatcher = req.getRequestDispatcher( "Admin" );
      dispatcher.forward( req, res );
      return;
    }

    // since servlets cannot have ivars, use a local variable (map) to
    // store information to pass to other methods of the servlet
    //
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put ( "request", req );
    map.put ( "response", res );

    // setup the output
    res.setContentType( "text/html" );
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
        error ( out, "Dart Administration", "Dart: no project found matching \"" + projectName + "\"", map );
        out.close();
        return;
        }
    } catch ( Exception e ) {
      logger.debug( projectName + ": error getting project" );
      error ( out, "Dart Administration", "Dart: Error accessing project \"" + projectName + "\"", map );
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
    ClientFinderBase clientFinder = new ClientFinderBase ( jaxorContext );

    // connect to the server database
    Connection serverConnection = project.getServer().getConnection();
    JaxorContextImpl serverJaxorContext = new JaxorContextImpl( serverConnection );
    UserFinderBase userFinder = new UserFinderBase(serverJaxorContext);
    
    // Setup the map of information that will be passed to the
    // template engine
    //
    HashMap<String, Object> root = new HashMap<String, Object>();
    root.put ( "serverName", project.getServer().getTitle() );
    root.put ( "projectName", projectName );
    root.put ( "projectProperties", project.getProperties() );
    root.put ( "request", req );
    root.put ( "clientFinder", clientFinder );
    root.put ( "userFinder", userFinder );

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
      root.put ( "writableParameters", wrapper.wrap ( new HashMap<Object,Object> ( parameters ) ) );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Could not wrap map", e );
      error ( out, "Dart Administration", "Dart: Failed to wrap parameters", map );
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
      // logger.info("Admin: no session");
    }

    // determine the date
    //
    java.util.Date date;
    date = new java.util.Date();
    root.put ( "date", date );
    
    // find the client specified in the query
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
      error ( out, "Dart Administration", "Dart: Failed to find or parse template: \"" + (String) map.get ( "templateName" ) + "\"", map );
      try { project.closeConnection ( connection ); } catch ( Exception e2 ) { }
      try { project.getServer().closeConnection ( serverConnection ); } catch ( Exception e2 ) { }
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
      try { project.closeConnection ( connection ); } catch ( Exception e ) { }
      try { project.getServer().closeConnection ( serverConnection ); } catch ( Exception e ) { }
    }
    out.close();
  }

  /**
   * doPost method.  This method is used for forms that modify
   * the database
   */
  public void doPost(HttpServletRequest req,
                    HttpServletResponse res)
    throws ServletException, IOException
  {
    Map parameters = req.getParameterMap();
    String command = req.getPathInfo();

    String projectName = getInitParameter("project");
    Project project = Server.getProject( projectName );

    // Get a handle to the realm for authentication
    UserRealm realm = project.getHttpServer().getRealm("Dart");

    // Principal representing a user
    Principal principal;

    // Grab a handle to the session
    HttpSession httpSession = req.getSession();
    
    // doPost() can be called for:
    //   1. Add a client property
    //   2. Remove a client property
    if (parameters.get("AddClientProperty") != null) {
      logger.debug("Adding a client property");
      // Verify that a clientid is specified
      if (! parameters.containsKey("clientid") ) {
        // redirect to the client  page
        logger.info("No clientid specified, redirect somewhere.");
        res.sendRedirect( req.getContextPath()
                          + req.getServletPath()
                          + req.getPathInfo()
                          + "?" + req.getQueryString() );
        return;
      }
      
      // validate user can modify the table
      principal = (Principal) httpSession.getAttribute("user");
      if (realm.isUserInRole( principal, "Dart.Administrator")
          || realm.isUserInRole( principal, projectName + ".Administrator")) {
        // connect to the database
        Connection connection = project.getConnection();
        JaxorContextImpl dbSession = new JaxorContextImpl( connection );
        ClientPropertyFinderBase clientPropertyFinder = new ClientPropertyFinderBase ( dbSession );

        // connect to the server database
        Connection serverConnection = project.getServer().getConnection();
        JaxorContextImpl serverJaxorContext = new JaxorContextImpl( serverConnection );
        UserFinderBase userFinder = new UserFinderBase(serverJaxorContext);

        String propertyName = ((String[])parameters.get("PropertyName"))[0];
        String propertyValue = ((String[])parameters.get("PropertyValue"))[0];
        String[] ids = (String[])parameters.get("clientid");
        ClientPropertyEntity cp = null;

        if (propertyName != null) {
          // The client property table is a multimap.  A given
          // property can appear multiple times with different
          // values.

          // ClientProperties are used to track expected submissions
          // and whom to notify when an expected submission is
          // missing.
          //
          // If a client is expected, it will have client property called
          //
          //       Expected.<TrackName>
          //
          // whose value is "true".
          //
          // If a user has been identified as a person to notify if a
          // submission is missing, the client will have a client property
          // called
          //
          //       Expected.<TrackName>.Notify.UserId
          //
          // which will have a value of a long.
          //
          // To simply adding users to the notification list, we map
          // userids to email addresses and vice versa.  So the admin
          // uses email addresses to specify users but we store
          // userids internally.
          if (propertyName.startsWith("Expected.")
              && propertyName.endsWith(".Notify.UserId")) {
            // need to map the property value from an email address to
            // a Dart UserId
            try {
              UserEntity user = userFinder.selectByEmail( propertyValue );
              propertyValue = new String(user.getUserId().toString());
            } catch (net.sourceforge.jaxor.EntityNotFoundException exc) {
              // cannot locate user by email
              logger.error("User to notify cannot be located.");

              // close the connection to the database
              try { project.closeConnection ( connection ); } catch (Exception e) {}
              try { project.getServer().closeConnection ( serverConnection ); } catch (Exception e) {}

              // redirect
              res.sendRedirect( req.getContextPath()
                                + req.getServletPath()
                                + req.getPathInfo()
                                + "?" + req.getQueryString() );
              return;
            }
          }
          
          // Check whether this property already exists, if so
          // redirect
          QueryParams qc = new QueryParams();
          qc.add(new net.sourceforge.jaxor.mappers.LongMapper(),
                 new Long(ids[0]) );
          qc.add(new net.sourceforge.jaxor.mappers.StringMapper(),
                 propertyName);
          qc.add(new net.sourceforge.jaxor.mappers.StringMapper(),
                 propertyValue);
          
          ClientPropertyList propertyList
            = clientPropertyFinder.find("where clientid=? and name=? and value=?", qc);
          
          if (propertyList.size() > 0) {
            logger.info("Client property already exists.");
            // close the connection to the database
            try { project.closeConnection ( connection ); } catch ( Exception e ) { }
            try { project.getServer().closeConnection ( serverConnection ); } catch ( Exception e ) { }
            
            // redirect to the Client page.
            res.sendRedirect( req.getContextPath()
                              + req.getServletPath()
                              + req.getPathInfo()
                              + "?" + req.getQueryString() );
            return;
          }

          // property entry does not exist yet, make a new one
          cp = clientPropertyFinder.newInstance();
          
          // set the property
          cp.setClientId(new Long(ids[0]));
          cp.setName(propertyName); 
          cp.setValue(propertyValue); 
          
          // Commit to the database
          try { dbSession.commit(); }
          catch (net.sourceforge.jaxor.util.SystemException e) {
            logger.error("Failed to update client property. " + e.getCauseList());
          }

          // refetch
          propertyList
            = clientPropertyFinder.selectByClientIdList( new Long(ids[0]));
        }
        
        // close the connection to the database
        try { project.closeConnection ( connection ); } catch ( Exception e ) { }
        try { project.getServer().closeConnection ( serverConnection ); } catch ( Exception e ) { }
      
        // redirect to the User page.
        res.sendRedirect( req.getContextPath()
                          + req.getServletPath()
                          + req.getPathInfo()
                          + "?" + req.getQueryString() );
      }
    } else if (parameters.get("RemoveClientProperty") != null) {
      logger.debug("Removing a client property");
      // Verify that a clientid is specified
      if (! parameters.containsKey("clientid") ) {
        // redirect to the client  page
        logger.info("No clientid specified, redirect somewhere.");
        res.sendRedirect( req.getContextPath()
                          + req.getServletPath()
                          + req.getPathInfo()
                          + "?" + req.getQueryString() );
        return;
      }
      
      // validate user can modify the table
      principal = (Principal) httpSession.getAttribute("user");
      if (realm.isUserInRole( principal, "Dart.Administrator")
          || realm.isUserInRole( principal, projectName + ".Administrator")) {
        // connect to the database
        Connection connection = project.getConnection();
        JaxorContextImpl dbSession = new JaxorContextImpl( connection );
        ClientPropertyFinderBase clientPropertyFinder = new ClientPropertyFinderBase ( dbSession );

        // connect to the server database
        Connection serverConnection = project.getServer().getConnection();
        JaxorContextImpl serverJaxorContext = new JaxorContextImpl( serverConnection );
        UserFinderBase userFinder = new UserFinderBase(serverJaxorContext);
        String propertyName = ((String[])parameters.get("PropertyName"))[0];
        String propertyValue = ((String[])parameters.get("PropertyValue"))[0];
        String[] ids = (String[])parameters.get("clientid");
        ClientPropertyEntity cp = null;

        if (propertyName != null) {
          // ClientProperties are used to track expected submissions
          // and whom to notify when an expected submission is
          // missing.
          //
          // If a client is expected, it will have client property called
          //
          //       Expected.<TrackName>
          //
          // whose value is "true".
          //
          // If a user has been identified as a person to notify if a
          // submission is missing, the client will have a client property
          // called
          //
          //       Expected.<TrackName>.Notify.UserId
          //
          // which will have a value of a long.
          //
          // To simply adding users to the notification list, we map
          // userids to email addresses and vice versa.  So the admin
          // uses email addresses to specify users but we store
          // userids internally.
          if (propertyName.startsWith("Expected.")
              && propertyName.endsWith(".Notify.UserId")) {
            // need to map the property value from an email address to
            // a Dart UserId
            try {
              UserEntity user = userFinder.selectByEmail( propertyValue );
              propertyValue = new String(user.getUserId().toString());
            } catch (net.sourceforge.jaxor.EntityNotFoundException exc) {
              // cannot locate user by email
              logger.error("User to notify cannot be located.");

              // close the connection to the database
              try { project.closeConnection ( connection ); } catch ( Exception e ) { }
              try { project.getServer().closeConnection ( serverConnection ); } catch ( Exception e ) { }

              // redirect
              res.sendRedirect( req.getContextPath()
                                + req.getServletPath()
                                + req.getPathInfo()
                                + "?" + req.getQueryString() );
              return;
            }
          }

          QueryParams qc = new QueryParams();
          qc.add(new net.sourceforge.jaxor.mappers.LongMapper(),
                 new Long(ids[0]) );
          qc.add(new net.sourceforge.jaxor.mappers.StringMapper(),
                 propertyName);
          qc.add(new net.sourceforge.jaxor.mappers.StringMapper(),
                 propertyValue);
        
          ClientPropertyList propertyList
            = clientPropertyFinder.find("where clientid=? and name=? and value=?", qc);

          if (propertyList.size() > 0) {
            cp = (ClientPropertyEntity) propertyList.toList().get(0);
            cp.delete();
          }
          
          // Commit to the database
          try { dbSession.commit(); }
          catch (net.sourceforge.jaxor.util.SystemException e) {
            logger.error("Failed to update client property. " + e.getCauseList());
          }
          
          // refetch the information
          ClientPropertyList properties
            = clientPropertyFinder.selectByClientIdList( new Long(ids[0]));
        }
        
        // close the connection to the database
        try { project.closeConnection ( connection ); } catch ( Exception e ) { }
        try { project.getServer().closeConnection ( serverConnection ); } catch ( Exception e ) { }
      
        // redirect to the User page.
        res.sendRedirect( req.getContextPath()
                          + req.getServletPath()
                          + req.getPathInfo()
                          + "?" + req.getQueryString() );
      }
    }
  }


  // locate the template specified on the URL pathinfo.  The parameter
  // "map" is used to pass information into and out of the method
  // (this servlet cannot have ivars since multiple threads may be
  // making different requests).
  // 
  void findTemplate ( HashMap<String, Object> map ) throws Exception {
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
