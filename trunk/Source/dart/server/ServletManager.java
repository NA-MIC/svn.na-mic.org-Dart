package dart.server;

import java.io.File;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import org.apache.log4j.*;
import org.mortbay.jetty.servlet.*;
import org.mortbay.http.HttpContext;

import dart.DartServer;
import dart.server.Container;
import dart.server.Project;
//import qed.server.QED;

/**
   Class to manage Servlets for a Container
*/
public class ServletManager {
  static Logger logger = Logger.getLogger ( ServletManager.class );   
  ServletHandler servlets = new ServletHandler();

  /**
     Add a servlet
     @param cl Class to add
     @param context Context in which to add the new Servlet
     @param properties Properties to make available to the Servlet
  */
  public void addServlet ( String cl, String context, Properties properties ) {
    logger.debug ( "adding servlet: " + cl + " context: " + context + " properties " + properties );
    ServletHolder servlet = servlets.addServlet ( context, cl );
    Iterator i = properties.keySet().iterator();
    while ( i.hasNext() ) {
      String key = (String) i.next();
      servlet.setInitParameter ( key, (String) properties.get ( key ) );
    }
  }

  /*
  public void addServlet ( Servlet servlet ) {
    logger.debug ( "adding servlet: " + servlet );
    servlets.addServlet ( "Foo", servlet );
  }
  */

  /**
     Start up the Manager
     @param p Container that contains the ServletManager
     @param context HttpContext in which to start the Servlets
  */
  public void start ( Container p, HttpContext context ) throws Exception {

    logger.debug ( p.getTitle() + ": Starting ServletManager" );
    // Loop through and add the project to each servlet
    ServletHolder[] l = servlets.getServlets();
    for ( int i = 0; i < l.length; i++ ) {
      // set an init parameter depending on whether p is a project or
      // a server
      if ( p instanceof Project ) {
        l[i].setInitParameter ( "project", p.getTitle() );
//       } else if ( p instanceof QED ) {
//         l[i].setInitParameter ( "qed", p.getTitle() );
      } else if ( p instanceof Server ) {
        l[i].setInitParameter ( "server", p.getTitle() );
      }
      

      // check to see if an additional classpath was specified for
      // this servlet.  The additional class path could be a
      // directory, a jar file or a url.  
      if (l[i].getInitParameter( "classpath" ) != null) {
        logger.debug( p.getTitle() + ": adding " + l[i].getInitParameter( "classpath" ) + " to classpath");
        context.addClassPath( l[i].getInitParameter( "classpath" ) );
      }
    }
    context.addHandler ( servlets );
  }

  /**
     Shutdown the manager
  */
  public void shutdown() throws Exception {
  }

  /**
     Return the status of the ServletManager
     @return String containing the status
  */
  public String toString () {
    StringBuffer buffer = new StringBuffer();
    return buffer.toString();
  }
}


/*
 * Log: $Log:$
 */
