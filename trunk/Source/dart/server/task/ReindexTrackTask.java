package dart.server.task;

import java.io.File;
import java.io.*;
import java.sql.*;
import java.net.URL;
import java.util.Properties;

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

public class ReindexTrackTask implements Task {
  static Logger logger = Logger.getLogger ( PlaceSubmissionInTrackTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    
    try {
      project.getTrackManager().reindexTracks();
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Failed to place submission" );
      throw e;
    } 
  }
}
