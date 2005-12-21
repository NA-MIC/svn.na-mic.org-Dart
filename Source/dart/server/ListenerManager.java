package dart.server;

import java.io.File;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import org.apache.log4j.*;

import dart.DartServer;
import dart.server.Container;
import dart.server.Project;
import dart.server.event.*;
import dart.server.listener.*;

/**
   Class to manage Servlets for a Container
*/
public class ListenerManager {
  static Logger logger = Logger.getLogger ( ListenerManager.class );   

  ArrayList listeners = new ArrayList ();

  public void addListener ( String cl, Properties properties, ArrayList listenTo ) {
    Listener listener = null;
    logger.debug ( "Adding listener: " + cl );
    try {
      // Create an instance
      listener = (Listener) Class.forName ( cl ).newInstance();
      listener.setProperties ( properties );
      listener.setListenTo ( listenTo );
      listeners.add ( listener );
    } catch ( Exception e ) {
      logger.error ( "Failed to create a listener of type: " + cl, e );
    }
  }

  public void start ( Container p ) throws Exception {
    logger.debug ( "Starting ListenerManager" );
  }


  /** Handle an event
   */
  public void triggerEvent ( Event e ) {
    Iterator it = listeners.iterator();
    String name = e.getClass().getName();
    while ( it.hasNext() ) {
      Listener l = (Listener) it.next();
      if ( l.canListenTo ( name ) ) {
        try {
          l.trigger ( e );
        } catch ( Exception ex ) {
          logger.error ( "Error triggering event " + e + " to Listener " + l, ex );
        }
      }
    }
  }

  /**
     Shutdown the manager
  */
  public void shutdown() throws Exception {
  }

  /**
     Return the status of the Manager
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
