package dart.server;

import java.io.File;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import org.apache.log4j.*;
import java.lang.reflect.*;
import dart.DartServer;
import dart.server.Container;
import dart.server.Project;
import dart.server.event.*;
import dart.server.listener.*;

/**
   Class to manage event listeners for a Container
*/
public class ListenerManager {
  static Logger logger = Logger.getLogger ( ListenerManager.class );   

  Project project = null;
  ArrayList listeners = new ArrayList ();

  public void addListener ( String cl, Properties properties ) {
    Listener listener = null;
    logger.debug ( "Adding listener: " + cl );
    try {
      // Create an instance
      listener = (Listener) Class.forName ( cl ).newInstance();
      listener.setProperties ( properties );
      listeners.add ( listener );
    } catch ( Exception e ) {
      logger.error ( "Failed to create a listener of type: " + cl, e );
    }
  }

  public void start ( Project p ) throws Exception {
    project = p;
    logger.debug ( "Starting ListenerManager" );
  }


  /** Handle an event
   */
  public void triggerEvent ( Event e ) {
    Iterator it = listeners.iterator();
    String name = e.getClass().getName();
    while ( it.hasNext() ) {
      Listener l = (Listener) it.next();
      try {
        
        // Try to find the method
        Method method = null;
        try {
          // See if the Listener can handle the event, silently fail.
          method = l.getClass().getDeclaredMethod ( "trigger", new Class[] { project.getClass(), Class.forName ( name ) } );
        } catch ( NoSuchMethodException methodException ) {
          logger.debug ( "No trigger method on " + l.getClass().getName() + " for " + name );
        }
        if ( method != null ) {
          method.invoke ( l, new Object[] { project, e } );
        }
      } catch ( Exception ex ) {
        logger.error ( "Error triggering event " + e + " to Listener " + l, ex );
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
