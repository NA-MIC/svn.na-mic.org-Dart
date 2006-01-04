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
  public void setProperties ( Properties p ) { properties = p; }

  public void trigger ( Project project, Event e ) throws Exception
  {
    throw new Exception ( "unhandled event: " + e );
  }
}

