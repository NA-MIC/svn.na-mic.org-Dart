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
  HashMap<String,Track> map = new HashMap<String,Track>();
  String defaultTrack;

  class CompareTrackPriority implements Comparator<Track> {
    public int compare(Track o1, Track o2) {
      long p1 = ((Track)o1).getPriority();
      long p2 = ((Track)o2).getPriority();      

      if (p1 < p2) return -1;
      else if (p1 > p2) return 1;
      else return 0;
    }
  };
  
  
  public void addTrack ( Track t ) {
    logger.debug ( "Adding Track: " + t );
    map.put ( t.getName(), t );

    // Assign each track a priority used for display purposes. By
    // default, tracks will be displayed in the order they were added
    // to the TrackManager (by default the order they are specified in
    // Project.xml).
    if (t.getPriority() == Track.DEFAULT_PRIORITY) {
      t.setPriority(map.size());
    }
  }

  public void setDefaultTrack ( String s ) { defaultTrack = s; }
  public String getDefaultTrack() { return defaultTrack; }
  
  public void start ( Project p ) throws Exception {
    project = p;
    logger.debug ( project.getTitle() + ": Starting TrackManager" );
    Iterator i = map.values().iterator();
    while ( i.hasNext() ) {
      Track track = (Track)i.next();
      track.setProject ( project );
    }
  }

  public HashMap getTracks() { return map; }

  /**
   * Return a list of track names in the order they should be
   * displayed
   */
  public String[] getTrackOrder() {

    // copy the map to an array so we can sort
    ArrayList<Track> trackObjectList = new ArrayList<Track> ( map.values());

    // sort the tracks on priority
    Track[] trackObjectArray = trackObjectList.toArray(new Track[0]);
    Arrays.sort(trackObjectArray, new CompareTrackPriority());

    // build a list of strings
    String[] trackorder = new String[trackObjectArray.length];
    for (int i=0; i < trackObjectArray.length; i++) {
      trackorder[i] = ((Track)trackObjectArray[i]).getName();
    }
    
    return trackorder;
  }
  
  public long getTrackId ( java.sql.Timestamp ts, String trackName ) {
    return ((Track)(map.get ( trackName ))).getTrackId ( ts );
  } 

  public long[] getTrackIds ( java.sql.Timestamp ts ) {
    // create or find the track intersectiong this timestamp
    long[] r = new long[map.size()];
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
  public void linkSubmission ( long SubmissionId ) throws Exception {
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
      try {
        logger.debug("Closing connection.");
        project.closeConnection ( connection );
        // connection.close(); 
      } catch ( Exception ex ) {}
    }
  }

  public void reindexTracks () {
    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    TrackFinderBase trackFinder = new TrackFinderBase ( session );
    try {
      // Validate the tracks, queue a task if the track is invalid
      TrackList list = trackFinder.findAll();
      TrackIterator i = list.iterator();
      while ( i.hasNext() ) {
        TrackEntity trackEntity = i.next();
        Track track = (Track) map.get ( trackEntity.getName() );

        boolean deleteTrack = false;
        // First case, a track was deleted
        if ( track == null ) {
          deleteTrack = true;
        } else {

          // If it doesn't contain any submissions
          SubmissionList submissionList = trackEntity.getSubmissionList();
          if ( submissionList.size() == 0 ) {
            deleteTrack = true;
          } else if ( !track.isValidTrack ( trackEntity ) ) {
            deleteTrack = true;
          }
        }

        if ( deleteTrack ) {
          logger.debug ( "Track is invalid: " + trackEntity.getTrackId() );
          // Queue this for deletion
          Properties prop = new Properties();
          prop.setProperty ( "TrackId", trackEntity.getTrackId().toString() );
          project.queueTask ( "dart.server.task.DeleteTrackTask", prop, 10 );
        }
      }
      session.commit();
    } catch ( Exception e ) {
      logger.error ( "Failed to link valid Tracks ", e );
    } finally { 
      try { 
        // connection.close();
        project.closeConnection ( connection );
      } catch ( Exception ex ) {}
    }
  }

  public void placeSubmission ( long SubmissionId , String trackName ) throws Exception {
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


