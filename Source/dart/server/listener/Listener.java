package dart.server.listener;

import java.util.*;
import org.apache.log4j.*;
import dart.DartServer;
import dart.server.Container;
import dart.server.Project;
import dart.server.event.*;
import dart.server.listener.*;

abstract public class Listener {
  static Logger logger = Logger.getLogger ( Listener.class );   
  Properties properties;
  ArrayList listenTo;
  public void setProperties ( Properties p ) { properties = p; }
  public void setListenTo ( ArrayList l ) { listenTo = l; };
  public boolean canListenTo ( Class c ) {
    return canListenTo ( c.getName() );
  }
  abstract public void trigger ( Event e ) throws Exception;
  public boolean canListenTo ( String name ) {
    Iterator it = listenTo.iterator();
    try {
      while ( it.hasNext() ) {
        if ( name.equalsIgnoreCase ( (String)it.next() ) ) {
          return true;
        }
      }
    } catch ( Exception e ) {
      logger.error ( "Error in canListenTo", e );
    }
    return false;
  }
}

