package dart.server.task;

import java.io.File;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;

import dart.server.Project;
import dart.server.TestProcessor;

public class SaveStatistics implements Task {
  static Logger logger = Logger.getLogger ( SaveStatistics.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    File file = new File ( project.getBaseDirectory(), "Statistics.txt" );
    try {
      OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
      project.getStats().store ( out, null );
    } catch ( Exception e ) {
      logger.error ( "Failed to save statistics", e );
      throw e;
    }
  }
}
