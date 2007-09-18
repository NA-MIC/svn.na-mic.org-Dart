package dart.server;

import org.apache.log4j.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import dart.server.*;
import dart.server.command.*;
import org.apache.xmlrpc.server.*;
import org.apache.xmlrpc.webserver.*;

public class CommandManager {
  static Logger logger = Logger.getLogger ( CommandManager.class );   
  Container project;
  HashMap<String,Object[]> map = new HashMap<String,Object[]>();
  String defaultTrack;
  XmlRpcServletServer xmlrpcServer = null;

  public XmlRpcServer getXmlRpcServer () { return xmlrpcServer; }

  public void addCommand ( String name, String ClassName, Properties properties ) {
    map.put ( name, new Object[] { ClassName, properties } );
  }

  public void start ( Container p ) throws Exception {
    HandlerMapping mapping = new HandlerMapping();
    project = p;
    xmlrpcServer = new XmlRpcServletServer();
    logger.debug ( project.getTitle() + ": Starting CommandManager" );
    Iterator i = map.keySet().iterator();
    Class a[] = { Container.class, Properties.class };
    while ( i.hasNext() ) {
      Command command;
      String name = (String)i.next();
      Object[] aa = map.get ( name );
      String ClassName = (String)aa[0];
      Properties properties = (Properties)aa[1];
      Object[] args = new Object[] { project, properties };
      try {
        Constructor constructor = Class.forName ( ClassName ).getConstructor ( a );
        command = (Command) constructor.newInstance ( args );
        logger.debug ( project.getTitle() + ": Starting Command: " + name );
        mapping.addHandler ( name, command );
      } catch ( Exception e ) {
        logger.error ( "Error creating command: " + name + " of type: " + ClassName, e );
      }
    }
    xmlrpcServer.setHandlerMapping ( mapping );
  }

  public void shutdown() throws Exception {
  }

  public String toString () {
    StringBuffer buffer = new StringBuffer();
    return buffer.toString();
  }
}


