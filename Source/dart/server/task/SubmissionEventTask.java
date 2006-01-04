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
    // Summarize the tests for a given submission
    String SubmissionId = properties.getProperty ( "SubmissionId" );
    SubmissionEvent event = new SubmissionEvent ( Long.parseLong( SubmissionId ) );
    project.getListenerManager().triggerEvent ( event );
  }      
}
