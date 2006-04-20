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
   *
   * @param recipients collection of Dart userids (currently email addresses)
   * @param defaultContacts collection of contacts specific to the
   * type of messenger (SMTP, IM, etc.)
   * @param subject short subject string
   * @param message content of the message
   */
  public void send(Collection recipients, Collection defaultContacts, String subject, String message) throws Exception {};
}

