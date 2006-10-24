package dart.server.task;

import java.io.File;
import java.io.*;
import java.sql.*;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import dart.server.Project;
import java.util.*;
import java.text.*;
import dart.server.event.*;

public class SubmissionEventTask implements Task {
  static Logger logger = Logger.getLogger ( SubmissionEventTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    String SubmissionId = properties.getProperty ( "SubmissionId" );
    String domains[] = properties.getProperty( "Domains" ).split(",");

    // Emit an event for each domain type in the submission
    Event event = null;
    int i;
    for (i=0; i < domains.length; ++i) {
      event = null;
      if (domains[i].equals("Build")) {
        // Emit a build event
        event = new BuildEvent ( Long.parseLong( SubmissionId ));
      } else if (domains[i].equals("Test")) {
        // Emit a test event
        event = new TestEvent ( Long.parseLong( SubmissionId ));
      } else if (domains[i].equals("Update")) {
        // Emit a update event
        event = new UpdateEvent ( Long.parseLong( SubmissionId ));
      } else if (domains[i].equals("Coverage")) {
        // Emit a coverage event
        event = new CoverageEvent ( Long.parseLong( SubmissionId ));
      } else if (domains[i].equals("Style")) {
        // Emit a style event
        event = new StyleEvent ( Long.parseLong( SubmissionId ));
      } else if (domains[i].equals("DynamicAnalysis")) {
        // Emit a dynamic analysis event
        event = new DynamicAnalysisEvent ( Long.parseLong( SubmissionId ));
      }

      if (event != null) {
        project.getListenerManager().triggerEvent ( event );
      }
    }
    
    
    // Emit a catchall submission event
    event = new SubmissionEvent ( Long.parseLong( SubmissionId ));
    project.getListenerManager().triggerEvent ( event );
  }      
}
