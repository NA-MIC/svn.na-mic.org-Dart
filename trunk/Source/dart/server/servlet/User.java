package dart.server.servlet;

import java.io.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.http.*;
import javax.servlet.*;

import java.sql.Connection;

import java.security.Principal;

import net.sourceforge.jaxor.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.*;
import freemarker.ext.beans.*;
import freemarker.ext.servlet.HttpSessionHashModel;

import org.apache.log4j.Logger;

import org.mortbay.http.UserRealm;
import org.mortbay.http.JDBCUserRealm;
import org.mortbay.http.HttpRequest;
  
import dart.server.Server;
import dart.server.Project;
import dart.server.wrap.*;


/**
 * Servlet to handle account creation, login, customization.
 *
 * Users are tracked within the servler container via session.  An
 * attribute on the session called "user" is the principal.
 *
 * To assist the user when creating their account, some information in
 * the form is tracked in the session. Specifically, the user's email,
 * first name, and last name.  This is solely to prevent the user from
 * re-entering this information should the account creation fail (due
 * to the user already existing or a bad password, etc.).  This
 * information other information (other than the principal) is removed
 * from the session hastily.
 *
 */
public class User extends HttpServlet {
  // May not have ivars as servlet engines are threaded
  static Logger logger = Logger.getLogger ( Server.class );   

  
  void error ( PrintWriter out, String title, String msg, HashMap map ) {
    String projectName = (String) map.get ( "projectName" );
    out.println( "<html><head><title>" + title + "</title></head>" );
    out.println( "<body>" );
    out.println( "<h1>Dart User" );
    out.println( "</h1>" );
    out.println( "<p>" );
    out.println( msg );
    out.println( "<p>" );
    out.println( "</body>" );
    out.println( "</html>" );
    return;
  }    

  /**
   * doGet() method.  Used for querying users.
   */
  public void doGet(HttpServletRequest req,
                    HttpServletResponse res)
    throws ServletException, IOException
  {
    // since servlets cannot have ivars, use a local variable (map) to
    // store information to pass to other methods of the servlet
    //
    HashMap map = new HashMap();
    map.put ( "request", req );
    map.put ( "response", res );

    // Setup the map of information that will be passed to the
    // template engine
    //
    HashMap root = new HashMap();
    root.put( "referer", req.getHeader("Referer"));

    // get any parameters on the request 
    Map parameters = req.getParameterMap();
    // cache the parameters for the template engine
    root.put("parameters", parameters);

    // put the http session in the data model for the template
    // engine. the session is wrapped so that it appears as a
    // hash. attributes of the session can be accessed as
    // ${session.foo} in the template.
    HttpSession httpSession = req.getSession(false);
    if (httpSession != null) {
      HttpSessionHashModel httpSessionModel
        = new HttpSessionHashModel(httpSession, ObjectWrapper.DEFAULT_WRAPPER);
      root.put("session", httpSessionModel);
    }
      
    // setup the output
    res.setContentType( "text/html" );
    PrintWriter out = res.getWriter();
    map.put ( "out", out );

    // 
    //
    Template template;
    String templateName = null;
    String serverName = null;
    String projectName = null;

    try {
      projectName = getInitParameter("project");

      if (projectName != null) {
        // find the server that contains this project
        Project project = Server.getProject(projectName);
        serverName = project.getServer().getTitle();

        root.put( "projectName", projectName);
      }

      // User templates are kept in the server resource area
      Configuration cfg = new Configuration();
      File resourcesDirectory
        = new File(Server.getServer(serverName).getBaseDirectory(),
                   "Templates");
      cfg.setDirectoryForTemplateLoading( resourcesDirectory );

      templateName = req.getPathInfo();
      template = cfg.getTemplate ( templateName + ".ftl");

    } catch ( Exception e ) {
      logger.error ( "Failed to find template", e );
      error ( out, "Dart User", "Dart: Failed to find or parse template: \""
              + templateName + "\"", map );
      out.close();
      return;
    }

    // Connect to the server database
    Server server = Server.getServer(serverName);
    Connection connection = server.getConnection();

    JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
    UserFinderBase userFinder = new UserFinderBase ( dbSession );

    // find the current user
    if (httpSession != null) {
      UserEntity user = null;
      Principal principal = (Principal) httpSession.getAttribute("user");
      if (principal != null) {
        try {
          user = userFinder.selectByEmail( principal.getName() );
          if (user != null) {
            root.put("user", user);
          }
        } catch ( EntityNotFoundException notfound ) {
          logger.warn("User not found");
        }
      } else {
        logger.warn("No authenticated user");
      }
    } else {
      logger.warn("No session available, cannot track user.");
    }
    
    // if a parameter logout exists, the log out the user and
    // redirect to the page they came from.
    if (parameters.get("Logout") != null) {
      // Logout the user
      //
      //
      Principal principal = (Principal) httpSession.getAttribute("user");
      if (principal != null) {
        logger.info("User \"" + principal.getName() + "\" logging out.");

        // log out of the realm
        UserRealm realm = server.getHttpServer().getRealm("Dart");
        realm.logout( principal );

        // clear out the session
        if (httpSession != null) {
          httpSession.setAttribute("user", null);
          httpSession.setAttribute("Email", null);
          httpSession.setAttribute("First", null);
          httpSession.setAttribute("Last", null);
          httpSession.setAttribute("DisplayName", null);
          httpSession.invalidate();
        }
      }

      // do the necessary clean up
      try { server.closeConnection ( connection ); } catch (Exception e) {}
      
      // redirect to the User Login page.
      res.sendRedirect( req.getHeader("Referer") );
      out.close();
      return;
    }

    // Pass the realm to the template engine
    UserRealm realm = server.getHttpServer().getRealm("Dart");
    BeansWrapper realmWrapper = new BeansWrapper();
    realmWrapper.setExposureLevel( BeansWrapper.EXPOSE_ALL );
    try {
      root.put ( "realm", realmWrapper.wrap(realm));
    } catch (Exception e) {}


    // finally run the template to generate the webpage
    //
    try {
      template.process ( root, out );
    } catch ( Exception e ) {
    } finally {
      try { server.closeConnection ( connection ); } catch ( Exception e ) { }
    }
    out.close();
  }

  /**
   * doPost method.  This method is used for performing a login,
   * validating a user, and displaying a user.
   */
  public void doPost(HttpServletRequest req,
                    HttpServletResponse res)
    throws ServletException, IOException
  {
    Map parameters = req.getParameterMap();
    String command = req.getPathInfo();

    // get the name of the server and the server itself
    String serverName = getInitParameter("server");

    // If there is no server name, then the servlet may have been
    // called from a project.  Get the project name and find the server.
    if (serverName == null) {
      // Check to see if we are user the servlet from a project
      String projectName = getInitParameter("project");
      
      if (projectName != null) {
        // find the server that contains this project
        Project project = Server.getProject(projectName);
        serverName = project.getServer().getTitle();
      }
    }

    // Get the server
    Server server = Server.getServer( serverName );

    // Get a handle to the realm for authentication
    UserRealm realm = server.getHttpServer().getRealm("Dart");

    // Principal representing a user
    Principal principal;

    // Grab a handle to the session
    HttpSession httpSession = req.getSession();

    // doPost() will attempt to move information from the request
    // parameters into the session.  First wipe out any old values in
    // the session.
    if (httpSession != null) {
      httpSession.setAttribute("Email", null);
      httpSession.setAttribute("First", null);
      httpSession.setAttribute("Last", null);
    }
      
    
    
    // doPost() can be called for:
    //    1. Logging in
    //    2. Creating a new account
    //    3. Modifying an account
    //
    if (parameters.get("Login") != null) {
      // Attempt to validate the user
      //
      //
      logger.info("User \"" + ((String[])parameters.get("Email"))[0]
                  + "\" logging in.");

      if (parameters.get("Email") != null) {
        principal
          = realm.authenticate( ((String[])parameters.get("Email"))[0],
                                ((String[])parameters.get("Password"))[0],
                                null);
        if (principal != null) {
          logger.info("User \"" + principal + "\" authenticated.");

          // user authenticated, store user in session. THis will
          // store the principal, the display name, and any other
          // properties for a user needed for customized rendering, etc.
          if (httpSession != null) {
            // cookies must have been enabled
            httpSession.setAttribute("user", principal);

            // connect to the database to query the user properties
            Connection connection = server.getConnection();
            JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
            UserFinderBase userFinder = new UserFinderBase ( dbSession );
            UserEntity user = userFinder.selectByEmail( principal.getName() );

            if (user != null) {
              httpSession.setAttribute("DisplayName", user.getFirstName());
              
              UserPropertyList upl = user.getUserPropertyList();
              UserPropertyIterator upit = upl.iterator();
              UserPropertyEntity up = null;
              while (upit.hasNext()) {
                up = upit.next();

                // User specified duration for plots
                if (up.getName().equals("PlotDuration")) {
                  httpSession.setAttribute("PlotDuration", up.getValue());
                }
                // other user properties
                // ...
              }
            }
            
            // close the connection to the database
            try { server.closeConnection ( connection ); } catch (Exception e) {}
                        
            // redirect to the user page
            res.sendRedirect( req.getContextPath()
                              + req.getServletPath() + "/User" );
          } else {
            // could not get or create a session, so cannot log in
          logger.warn("User \"" + ((String[])parameters.get("Email"))[0]
                      + "\" authenticated but could not be tracked.");

          // redirect to the login page displaying an error
          if (httpSession != null) {
            httpSession.setAttribute("Email",
                                     ((String[])parameters.get("Email"))[0]);
          }
          res.sendRedirect( req.getRequestURI() + "?error=1");
          }

        } else {
          logger.warn("User \"" + ((String[])parameters.get("Email"))[0]
                      + "\" could not be authenticated.");

          if (httpSession != null) {
            httpSession.setAttribute("Email",
                                     ((String[])parameters.get("Email"))[0]);
          }
          
          // redirect to the login page displaying an error
          res.sendRedirect( req.getRequestURI() + "?error=2" );
        }
     }

    } else if (parameters.get("CreateUser") != null) {
      // Create a new user account
      //
      //
      logger.info("Creating new user account");
      
      String msg = "";
      String msgSpacer = "";

      if (httpSession == null) {
        // couldn't create session, will not be able to track user.
        msg = msg + msgSpacer + "error=1";
        msgSpacer = "&";
      }
      
      // Connect to the server database
      Connection connection = server.getConnection();

      JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
      UserFinderBase userFinder = new UserFinderBase ( dbSession );
        
      // Check to see if username already exists.  If it does, then redirect
      // with an error.  Otherwise, continue.
      //
      if (parameters.get("Email") != null) {
        // Look for the user
        UserEntity user = null;
        try {
          user = userFinder.selectByEmail( ((String[])parameters.get("Email"))[0]);
        } catch ( Exception e ) {}
        
        if (user != null) {
          // user already exists, redirect to the login page displaying
          // an error
          logger.warn("Cannot create user account. User already exists.");
          msg = msg + msgSpacer + "error=3";
          msgSpacer = "&";
          if (httpSession != null) {
            httpSession.setAttribute("Email",
                                     ((String[])parameters.get("Email"))[0]);
          }
        }
      }
      
      // Validate input
      if (parameters.get("Email") == null
          || ((String[])parameters.get("Email"))[0].equals("")) {
        msg = msg + msgSpacer + "error=4";
        msgSpacer = "&";
      } else {
        if (httpSession != null) {
          httpSession.setAttribute("Email",
                                   ((String[])parameters.get("Email"))[0]);
        }
      }

      if (parameters.get("Password") == null
          || ((String[])parameters.get("Password"))[0].equals("")) {
        msg = msg + msgSpacer + "error=5";
        msgSpacer = "&";
      }

      if (parameters.get("Retype") == null
          || ((String[])parameters.get("Retype"))[0].equals("")) {
        msg = msg + msgSpacer + "error=6";
        msgSpacer = "&";
      }

      if (parameters.get("Password") != null
          && parameters.get("Retype") != null) {
        if (!((String[])parameters.get("Password"))[0].equals(((String[])parameters.get("Retype"))[0])) {
        msg = msg + msgSpacer + "error=7";
        msgSpacer = "&";
        }
      }
      
      if (parameters.get("First") == null
          || ((String[])parameters.get("First"))[0].equals("")) {
        msg = msg + msgSpacer + "error=8";
        msgSpacer = "&";
      } else {
        if (httpSession != null) {
          httpSession.setAttribute("First",
                                   ((String[])parameters.get("First"))[0]);
        }
      }

      if (parameters.get("Last") == null
          || ((String[])parameters.get("Last"))[0].equals("")) {
        msg = msg + msgSpacer + "error=9";
        msgSpacer = "&";
      } else {
        if (httpSession != null) {
          httpSession.setAttribute("Last",
                                   ((String[])parameters.get("Last"))[0]);
        }
      }
      
      // if there are any errors, redirect
      if (!msg.equals("")) {
        // redirect to the login page displaying an error
        res.sendRedirect( req.getRequestURI() + "?" + msg);
      } else {
        logger.info("User information validated. Creating user.");

        // Create a UserEntity 
        UserEntity user = userFinder.newInstance();
        user.setEmail(((String[])parameters.get("Email"))[0]);
        user.setPassword(((String[])parameters.get("Password"))[0]);
        user.setFirstName(((String[])parameters.get("First"))[0]);
        user.setLastName(((String[])parameters.get("Last"))[0]);
        user.setActive( "1" );

        // add a user roles? Default to general guest or user?
        
        // commit the additions to the database
        try { dbSession.commit(); }
        catch (net.sourceforge.jaxor.util.SystemException e) {
          logger.error("Failed to create user. " + e.getCauseList());
        }
        // get the user back from the database (db fills in UserId)
        user
          = userFinder.selectByEmail(((String[])parameters.get("Email"))[0]);

        logger.info("User committed.");
        
        // authenticate the user
        principal
          = realm.authenticate( ((String[])parameters.get("Email"))[0],
                                ((String[])parameters.get("Password"))[0],
                                null);
        if (principal != null) {
          if (httpSession != null) {
            httpSession.setAttribute("user", principal);
          
            // redirect to the User page.
            res.sendRedirect( req.getContextPath()
                              + req.getServletPath() + "/User" );
            logger.info("User authenticated.");
          }
        } else {
          // error and redirect to the creation page
          logger.warn("Cannot authenticate user");
        }
        
      }

      try { server.closeConnection ( connection ); } catch (Exception e) {}

    } else if (parameters.get("UpdateUser") != null) {
      // Modify a user
      //
      //
      logger.info("Modifying a user account");

      String msg = "";
      String msgSpacer = "";

      // Connect to the server database
      Connection connection = server.getConnection();

      JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
      UserFinderBase userFinder = new UserFinderBase ( dbSession );

      // Get the current user
      principal = (Principal) httpSession.getAttribute("user");
      UserEntity user = userFinder.selectByEmail( principal.getName() );

      boolean passwordModified = false;
      boolean firstNameModified = false;
      boolean lastNameModified = false;

      // recache the Email address in the session
      httpSession.setAttribute("Email",
                               principal.getName());
      
      // Check for fields that were modified
      //
      //
      if (parameters.get("Password") != null
          && parameters.get("Retype") != null) {
        // password fileds are not empty
        if (!user.getPassword().equals(((String[])parameters.get("Password"))[0])) {
          // password is different from cached password
          if (!((String[])parameters.get("Password"))[0].equals(((String[])parameters.get("Retype"))[0])) {
            // password and retype do not match, don't change password
            msg = msg + msgSpacer + "error=1";
            msgSpacer = "&";
          } else {
            passwordModified = true;
          }
        }
      }

      if (parameters.get("First") == null
          || ((String[])parameters.get("First"))[0].equals("")) {
        // empty first name, do not con't change first name
        msg = msg + msgSpacer + "error=2";
        msgSpacer = "&";
      } else if (!user.getFirstName().equals(((String[])parameters.get("First"))[0]) ){
        firstNameModified = true;
      }

      if (parameters.get("Last") == null
          || ((String[])parameters.get("Last"))[0].equals("")) {
        // empty last name, do not con't change last name
        msg = msg + msgSpacer + "error=3";
        msgSpacer = "&";
      } else if (!user.getLastName().equals(((String[])parameters.get("Last"))[0]) ) {
        lastNameModified = true;
      }

      // if there are any errors, redirect
      if (!msg.equals("")) {
        // redirect to the login page displaying an error
        res.sendRedirect( req.getRequestURI() + "?" + msg);
      } else {
        logger.info("User information validated. Modifying user.");

        if (passwordModified) {
          user.setPassword( ((String[])parameters.get("Password"))[0] );
        }

        if (firstNameModified) {
          user.setFirstName( ((String[])parameters.get("First"))[0] );
          httpSession.setAttribute("First",
                                   ((String[])parameters.get("First"))[0]);
        }

        if (lastNameModified) {
          user.setLastName( ((String[])parameters.get("Last"))[0] );
          httpSession.setAttribute("Last",
                                   ((String[])parameters.get("Last"))[0]);
        }

        
        // logger.info("User before commit: " + user);
        try { dbSession.commit(); }
        catch (net.sourceforge.jaxor.util.SystemException e) {
          logger.error("Failed to modify user. " + e.getCauseList());
        }
        // get the user back from the database (db fills in UserId)
        user
          = userFinder.selectByEmail(principal.getName());

        // logger.info("User committed. "+ user);

        logger.info("Logging out " + principal.getName());
        realm.logout( principal );

        // authenticate the user
        logger.info("Principal = " + principal);
        principal
          = realm.authenticate( user.getEmail(),
                                user.getPassword(), 
                                null);
        if (principal != null) {
          if (httpSession != null) {
            httpSession.setAttribute("user", principal);
          
            // redirect to the User page.
            res.sendRedirect( req.getContextPath()
                              + req.getServletPath() + "/User" );
            logger.info("User authenticated.");
          }
        } else {
          // error and redirect to the creation page
          logger.warn("Cannot authenticate user. " + principal + ", " + user);
        }
      }        

      try { server.closeConnection ( connection ); } catch (Exception e) {}
      
    } else if (parameters.get("AddRepositoryId") != null) {
      // Modify a user
      //
      //
      logger.info("Adding a repository id");
      
      // Connect to the server database
      Connection connection = server.getConnection();

      JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
      UserFinderBase userFinder = new UserFinderBase ( dbSession );
      UserPropertyFinderBase userPropertyFinder
        = new UserPropertyFinderBase( dbSession );

      // Get the current user
      principal = (Principal) httpSession.getAttribute("user");
      UserEntity user = userFinder.selectByEmail( principal.getName() );

      // verify the input
      if (parameters.get("ProjectName") != null
          && parameters.get("RepositoryId") != null) {

        // update the user properties
        UserPropertyEntity property
          = userPropertyFinder.newInstance();
        property.setUserId( user.getUserId() );
        property.setName( ((String[])parameters.get("ProjectName"))[0]+".RepositoryId");
        property.setValue( ((String[])parameters.get("RepositoryId"))[0] );
        
        try { dbSession.commit(); }
        catch (net.sourceforge.jaxor.util.SystemException e) {
          logger.error("Failed to update repository ids. " + e.getCauseList());
        }

        // refetch the information
        UserPropertyList properties = user.getUserPropertyList();
        // logger.info(properties);
      }
      
      // close the connection to the database
      try { server.closeConnection ( connection ); } catch (Exception e) {}
      
      // redirect to the User page.
      res.sendRedirect( req.getContextPath()
                        + req.getServletPath() + "/User" );

    } else if (parameters.get("RemoveRepositoryId") != null) {
      // Modify a user
      //
      //
      logger.info("Removing a repository id");
      
      // Connect to the server database
      Connection connection = server.getConnection();

      JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
      UserFinderBase userFinder = new UserFinderBase ( dbSession );
      UserPropertyFinderBase userPropertyFinder
        = new UserPropertyFinderBase( dbSession );

      // Get the current user
      principal = (Principal) httpSession.getAttribute("user");
      UserEntity user = userFinder.selectByEmail( principal.getName() );

      // verify the input
      if (parameters.get("ProjectName") != null
          && parameters.get("RepositoryId") != null) {

        // update the user properties
        QueryParams qc = new QueryParams();
        qc.add(new net.sourceforge.jaxor.mappers.LongMapper(),
               user.getUserId() );
        qc.add(new net.sourceforge.jaxor.mappers.StringMapper(),
               ((String[])parameters.get("ProjectName"))[0] + ".RepositoryId");
        qc.add(new net.sourceforge.jaxor.mappers.StringMapper(),
               ((String[])parameters.get("RepositoryId"))[0]);
        
        UserPropertyList propertyList
          = userPropertyFinder.find("where userid=? and name=? and value=?",
                                    qc);

        if (propertyList.size() > 0) {
          UserPropertyEntity p = (UserPropertyEntity) propertyList.toList().get(0);
          p.delete();
        }
          
        try { dbSession.commit(); }
        catch (net.sourceforge.jaxor.util.SystemException e) {
          logger.error("Failed to update repository ids. " + e.getCauseList());
        }

        // refetch the information
        UserPropertyList properties = user.getUserPropertyList();
        // logger.info(properties);
      }
      
      // close the connection to the database
      try { server.closeConnection ( connection ); } catch (Exception e) {}
      
      // redirect to the User page.
      res.sendRedirect( req.getContextPath()
                        + req.getServletPath() + "/User" );
    } else if (parameters.get("Logout") != null) {
      // Logout the user
      //
      //
      principal = (Principal) httpSession.getAttribute("user");
      if (principal != null) {
        logger.info("User \"" + principal.getName() + "\" logging out.");
      
        realm.logout( principal );
        if (httpSession != null) {
          httpSession.setAttribute("user", null);
          httpSession.setAttribute("Email", null);
          httpSession.setAttribute("First", null);
          httpSession.setAttribute("Last", null);
          httpSession.setAttribute("DisplayName", null);
          httpSession.invalidate();
        }
      }

      // redirect to the User Login page.
      res.sendRedirect( req.getContextPath()
                        + req.getServletPath() + "/UserLogin" );
    } else {
      logger.warn("Dart User: unknown action");
    }
  }

}
