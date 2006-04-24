package dart.server.command;

import dart.server.*;
import java.io.File;
import java.io.*;
import java.util.Properties;
import java.util.zip.*;

import org.apache.log4j.Logger;

/*
 * Class to handle server administration commands
 * @author Dan Blezek
 */
public class ServerAdministration implements Command {
  Server server = null;
  static Logger logger = Logger.getLogger ( ServerAdministration.class );   
  Properties properties;

  public ServerAdministration ( Container c, Properties p ) throws Exception {
    if ( c instanceof Server ) {
      server = (Server)c;
      properties = p;
      logger.debug ( "Created ServerAdministration Command" );
    } else {
      throw new Exception ( "Submit can only work on Servers" );
    }
  }


  /**
     Shutdown the server
     @param username Used to validate shutdown rights
     @param password Used to validate shutdown rights
  */
  public int shutdown ( String username, String password ) {
    logger.info ( "Shutdown command received from " + username );
    server.doShutdown();
    System.exit ( 0 );
    return 0;
  }
    
  /**
     Handle the <code>getStatus</code> XML-RPC method
  */
  public String getStatus () {
    logger.debug ( "getStatus called" );
    return server.getStatus();
  }

  /**
     Handle the <code>getSchedulerStatus</code> XML-RPC method
  */
  public String getSchedulerStatus () {
    logger.debug ( "getSchedulerStatus called" );
    return server.getSchedulerStatus();
  }


  /**
   * Refresh the server resources
   * Handle a request to refresh the server resources
   * @see Server#refreshResources
   */
  public boolean refresh () {
    server.refreshResources();
    return true;
  }
}

