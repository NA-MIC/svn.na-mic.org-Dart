package dart.server.task;

import java.io.File;
import java.io.*;
import java.net.URL;
import java.text.*;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import dart.server.Project;
import dart.server.TestProcessor;

public class GarbageCollectionTask implements Task {
  static Logger logger = Logger.getLogger ( GarbageCollectionTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    logger.debug ( project.getTitle() + ": Execute GarbageCollectionTask" );
    DecimalFormat format = new DecimalFormat ( "0.00" );
    logger.debug ( project.getTitle() + ": Before collection: " + format.format ( Runtime.getRuntime().totalMemory() / 1048576.0 ) + "M / " + format.format ( Runtime.getRuntime().maxMemory() / 1048576.0 ) + "M" );
    System.gc();
    logger.debug ( project.getTitle() + ": After collection: " + format.format ( Runtime.getRuntime().totalMemory() / 1048576.0 ) + "M / " + format.format ( Runtime.getRuntime().maxMemory() / 1048576.0 ) + "M" );
  }
}
