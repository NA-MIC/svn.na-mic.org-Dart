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
import dart.server.messenger.*;

/**
   Class to manage various messaging services for a Container
*/
public class MessengerManager {
  static Logger logger = Logger.getLogger ( MessengerManager.class );   

  Project project = null;
  HashMap messengers = new HashMap ();

  public void addMessenger ( String name, String cl, Properties properties ) {
    Messenger messenger = null;
    logger.debug ( "Adding messenger: " + name + "(" + cl  + ")");
    try {
      // Create an instance
      messenger = (Messenger) Class.forName ( cl ).newInstance();
      messenger.setName( name );
      messenger.setProperties ( properties );
      messengers.put ( name, messenger );
    } catch ( Exception e ) {
      logger.error ( "Failed to create a messenger of type: " + cl, e );
    }
  }

  public Messenger getMessenger( String name ) {
    Messenger messenger = null;

    messenger = (Messenger) messengers.get(name);
    return messenger;
  }
  
  public void start ( Project p ) throws Exception {
    project = p;
    logger.debug ( "Starting MessengerManager" );
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
