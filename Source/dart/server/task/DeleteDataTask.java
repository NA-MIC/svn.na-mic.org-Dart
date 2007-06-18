package dart.server.task;

import java.io.File;
import java.io.*;
import java.sql.*;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;

import dart.server.Project;
import dart.server.TestProcessor;
import dart.server.wrap.*;
import net.sourceforge.jaxor.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.util.*;
import java.text.*;

public class DeleteDataTask implements Task {
  static Logger logger = Logger.getLogger ( DeleteDataTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    if ( properties == null ) {
      logger.warn ( "Null properties" );
    }

    String ResultValue = properties.getProperty ( "ResultValue", null );
    if ( ResultValue == null ) {
      return;
    }

    // Delete the file if no one references it
    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    ResultFinderBase resultFinder = new ResultFinderBase ( session );
    ResultResultSet i = null;
    try {    
      i = resultFinder.selectByValueResultSet ( ResultValue );
      if ( i.hasNext() ) {
        logger.debug ( "Not deleting data, found active reference" );
        return;
      } else {
        logger.debug ( "Deleting result data: " + ResultValue );
        File file = new File ( project.getDataDirectory() + File.separator + ResultValue );
        if ( !file.exists() ) {
          logger.error ( project.getTitle() + ": File does not exist: " + file.getPath() );
          return;
        }
        if ( !file.delete() ) {
          logger.error ( "Failed to delete file: " + file.getPath() );
          return;
        }
      }
    } catch ( Exception e ) {
      logger.error ( "Failed to delete data", e );
      throw e;
    } finally {
      if ( i != null ) { 
        i.close();
      }
      logger.debug("Closing connection.");
      // connection.close();
      project.closeConnection ( connection );
      
    }
  }
}
