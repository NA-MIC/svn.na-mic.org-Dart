package dart.server.command;

import dart.server.*;
import java.io.File;
import java.io.*;
import java.util.Properties;
import java.util.zip.*;

import org.apache.log4j.Logger;

/*
 */
public class ProjectAdministration implements Command {
  Project project = null;
  static Logger logger = Logger.getLogger ( ProjectAdministration.class );   
  Properties properties;
  public ProjectAdministration ( Container p, Properties prop ) throws Exception {
    if ( p instanceof Project ) {
      project = (Project)p;
      properties = prop;
      logger.debug ( project.getTitle() + ": Created ProjectAdministration Command" );
    } else {
      throw new Exception ( "Submit can only work on Projects" );
    }
  }

  public boolean runSQL ( String username, String password, String sql ) {
    logger.debug ( "Starting runSQL" );
    if ( project.getAdministratorUsername().equals ( "" )
         || project.getAdministratorPassword().equals ( "" ) ) {
      logger.debug ( "Empty username and/or password" );
      return false;
    }

    logger.debug ( "Username param: " + username );
    logger.debug ( "Password param: " + password );
    logger.debug ( "Username project: " + project.getAdministratorUsername() );
    logger.debug ( "Password project: " + project.getAdministratorPassword() );


    // Validate username/password
    boolean hasError = true;
    if ( project.getAdministratorUsername().equals ( username )
         && project.getAdministratorPassword().equals ( password ) ) {
      // Run the SQL
      try {
        hasError = false;
        logger.debug ( "Executing SQL: " + sql );
        project.executeSQL ( new StringReader ( sql ) );
      } catch ( Exception e ) {
        hasError = true;
      }
    } else {
      logger.debug ( "Failed to validate username/password" );
    }
    return hasError;
  }
}
