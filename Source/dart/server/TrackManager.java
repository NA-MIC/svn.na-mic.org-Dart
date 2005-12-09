package dart.server;

import org.apache.log4j.*;
import java.sql.*;
import java.util.*;
import dart.server.wrap.*;
import dart.server.*;
import dart.server.track.*;
import net.sourceforge.jaxor.*;

public class TrackManager {
  static Logger logger = Logger.getLogger ( TrackManager.class );   
  Project project;
  HashMap map = new HashMap();
  String defaultTrack;

  public void addTrack ( Track t ) {
    logger.debug ( "Adding Track: " + t );
    map.put ( t.getName(), t );
  }

  public void setDefaultTrack ( String s ) { defaultTrack = s; }

  public void start ( Project p ) throws Exception {
    project = p;
    logger.debug ( project.getTitle() + ": Starting TrackManager" );
    Iterator i = map.values().iterator();
    while ( i.hasNext() ) {
      Track track = (Track)i.next();
      track.setProject ( project );
    }
  }

  public int getTrackId ( java.sql.Timestamp ts, String trackName ) {
    return ((Track)(map.get ( trackName ))).getTrackId ( ts );
  } 

  public int[] getTrackIds ( java.sql.Timestamp ts ) {
    // create or find the track intersectiong this timestamp
    int[] r = new int[map.size()];
    Iterator i = map.values().iterator();
    int idx = 0;
    while ( i.hasNext() ) {
      Track track = (Track)i.next();
      r[idx] = track.getTrackId ( ts );
      idx++;
    }
    return r;
  }
  /**
     Updates the linked list of submissions to have easy access to
     next and last submissions from this Client of this type.
  */
  public void linkSubmission ( int SubmissionId ) throws Exception {
    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    try {
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
      SubmissionEntity submission = submissionFinder.selectBySubmissionId ( new Long ( SubmissionId ) );

      SubmissionEntity lastEntity, nextEntity;
      SubmissionList l;
      l = submission.selectLastSubmissions();
      if ( l.size() > 0 ) {
        lastEntity = l.get ( 0 );
        lastEntity.setNextSubmissionId ( submission.getSubmissionId() );
        submission.setLastSubmissionId ( lastEntity.getSubmissionId() );
      }
      l = submission.selectNextSubmissions();
      if ( l.size() > 0 ) {
        nextEntity = l.get ( 0 );
        nextEntity.setLastSubmissionId ( submission.getSubmissionId() );
        submission.setNextSubmissionId ( nextEntity.getSubmissionId() );
      }
      session.commit();
    } catch ( Exception e ) {
      logger.error ( "Failed to link Submission: " + SubmissionId );
    } finally { 
      try { connection.close(); } catch ( Exception ex ) {}
    }
  }
  
  public void placeSubmission ( int SubmissionId , String trackName ) throws Exception {
    linkSubmission ( SubmissionId );
    // Find the Track with the Type name
    Track track;
    track = (Track) map.get ( trackName );
    if ( track == null ) {
      track = (Track) map.get ( defaultTrack );      
    }
    if ( track == null ) {
      String e = project.getTitle() + ": not able to place submission, Track " + trackName + " was not found";
      logger.error ( e );
      throw new Exception ( e );
    }
    if ( !track.placeSubmission ( SubmissionId ) ) {
      String e = project.getTitle() + ": failed to place submission, SubmissionId " + SubmissionId;
      logger.error ( e );
      throw new Exception ( e );
    }
  }


  public void shutdown() throws Exception {
  }

  public String toString () {
    StringBuffer buffer = new StringBuffer();
    return buffer.toString();
  }
}


