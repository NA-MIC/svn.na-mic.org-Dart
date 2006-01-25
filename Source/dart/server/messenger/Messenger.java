package dart.server.messenger;

import java.util.*;
import org.apache.log4j.*;
import dart.DartServer;
import dart.server.Container;
import dart.server.Project;
import dart.server.event.*;
import dart.server.messenger.*;

abstract public class Messenger {
  static Logger logger = Logger.getLogger ( Messenger.class );   
  Properties properties;
  String name;
  public void setProperties ( Properties p ) { properties = p; }
  public Properties getProperties() { return properties; }

  public void setName ( String s ) { name = s; }
  public String getName () { return name; }

  /**
   * Send a message to a list recipients.
   */
  public void send(Collection recipients, String subject, String message) throws Exception {};
}

