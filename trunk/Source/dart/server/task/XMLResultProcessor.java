package dart.server.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;

import dart.server.Project;
import dart.server.TestProcessor;
import dart.server.TestProxy;

public class XMLResultProcessor implements Task {
  static Logger logger = Logger.getLogger ( XMLResultProcessor.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    URL file = new URL ( properties.getProperty ( "URL" ) );

    URL rules = Project.class.getClassLoader().getResource ( "dart/Resources/Server/TestProcessorRules.xml" );
    // logger.debug ( "Found TestProcessingRules.xml at: " + rules );
    
    // Should we delete when digestion is completed?
    boolean deleteWhenDigested = Boolean.valueOf ( properties.getProperty ( "DeleteWhenDigested", "false" ) ).booleanValue();

    // Try to create the project
    Digester digester = DigesterLoader.createDigester ( rules );

    logger.debug ( "Processed rules, starting to parse" );
    TestProcessor t = new TestProcessor ( project );
    t.setMaxTests ( Integer.parseInt ( project.getProperties().getProperty ( "MaxTestsPerSubmission", "-1" ) ) );
    digester.push ( t );
    project.incrementStatistic ( "Submissions" );
    InputStream input = file.openStream();

    if ( !properties.containsKey ( "UnCompressed" ) ) {
      input = new GZIPInputStream ( input );
    } else {
      logger.debug ( project.getTitle() + ": uncompressed " + file  );
      input = new BufferedInputStream ( input );
    }
      
    boolean hasError = false;
    try {
      digester.parse ( input );
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Failed to parse file: " + file, e );
      hasError = true;
      throw e;
    } finally {
      t.processDelayed();
      t.queueSummary();
      logger.debug ( project.getTitle() + ": DeleteWhenDigested " + deleteWhenDigested );
      if ( deleteWhenDigested & !hasError ) {
        logger.debug ( project.getTitle() + ": Attempting to delete " + file.toString() );
        if ( file.toString().startsWith ( "file:" ) ) {
          File f = new File ( new URI ( file.toString() ) );
          f.delete();
        }
      } else {
        // Move to the "Failed" subdirectory
        try {
          File f = new File ( new URI ( file.toString() ) );
          File newFile = new File ( f.getParentFile(), "Failed" + File.separator + f.getName() );
          logger.debug ( project.getTitle() + ": Moving " + f.toString() + " to " + newFile.toString() );
          f.renameTo ( newFile );
        } catch ( Exception e ) {
          logger.error ( "Failed to move " + file + " to Failed directory", e );
        }
      } 
    }
  }
}
