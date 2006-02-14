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

public class DeleteTrackTask implements Task {
  static Logger logger = Logger.getLogger ( DeleteTrackTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    // Delete the given track, and reindex all it's submissions
    Long TrackId = Long.valueOf ( properties.getProperty ( "TrackId" ) );
    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );

    try {
      logger.debug ( "Finding track: " + TrackId );
      TrackFinderBase trackFinder = new TrackFinderBase ( session );
      TrackEntity track = trackFinder.selectByPrimaryKey ( TrackId );
      
      // Patch up the linked list
      Long LastTrackId = track.getLastTrackId();
      Long NextTrackId = track.getNextTrackId();
      TrackList list = null;
      if ( NextTrackId != null ) {
        list = track.selectNextTrackList();
        if ( list.size() > 0 ) {
          list.get ( 0 ).setLastTrackId ( LastTrackId );
        }
      }
      if ( LastTrackId != null ) {
        list = track.selectLastTrackList();
        if ( list.size() > 0 ) {
          list.get ( 0 ).setNextTrackId ( LastTrackId );
        }
      }
      // Clear all the submissions out, and queue
      SubmissionList submissionList = track.getSubmissionList();
      SubmissionIterator submissions = submissionList.iterator();
      while ( submissions.hasNext() ) {
        SubmissionEntity submission = submissions.next();
        submission.setTrackId ( null );
        // Queue
        Properties prop = new Properties();
        prop.setProperty ( "SubmissionId", submission.getSubmissionId().toString() );
        prop.setProperty ( "TrackName", submission.getType().toString() );
        project.queueTask ( "dart.server.task.PlaceSubmissionInTrackTask", prop, 2 );
        logger.debug ( "Queued PlaceSubmissionInTrackTask task for " + submission.getSubmissionId() );
      }
      track.delete();
      session.commit();
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Failed to place delete track", e );
      throw e;
    } finally {
      try { connection.close(); } catch ( Exception e ) { }
    }      
  }
}
