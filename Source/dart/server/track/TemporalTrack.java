package dart.server.track;

import org.apache.log4j.*;
import java.lang.*;
import java.text.*;
import java.util.*;
import dart.server.wrap.*;
import dart.server.*;
import net.sourceforge.jaxor.*;
import net.sourceforge.jaxor.mappers.*;
import java.sql.Connection;

public class TemporalTrack implements Track {
  static Logger logger = Logger.getLogger ( TemporalTrack.class );   
  double duration = 24.0;
  String name = "Track";
  Date start;
  long priority = Track.DEFAULT_PRIORITY;
  String defaultSortBy = "buildName";
  String defaultOrder = "ascending";
  Project project;
  DateFormat format = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM );

  public String getName() { return name; }
  public TemporalTrack () {}
  public void setName ( String n ) { name = n; }
  public void setDuration ( String d ) { 
    duration = Double.parseDouble ( d );
  }
  public void setPriority(String p) { priority = Long.parseLong(p); };
  public void setPriority(long p) { priority = p; };
  public long getPriority() { return priority; };
  public void setDefaultSortBy(String s) { defaultSortBy = s; }
  public String getDefaultSortBy() { return defaultSortBy; }
  public String getDefaultOrder() { return defaultOrder; }
  public void setDefaultOrder(String s) { defaultOrder = s; }
  
  public void setStart ( String s ) { 
    int[] styles = { DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL };

    // Try to parse date/time first
    for ( int i = 0; i < styles.length; i++ ) {
      for ( int j = 0; j < styles.length; j++ ) {
        try {
          start = DateFormat.getDateTimeInstance( styles[i], styles[j] ).parse ( s );
          break;
        } catch ( Exception e ) {
          logger.debug ( "Did not parse date/time: " + i + ", " + j );
        }
      }
    }
    // Now go for just date or just time
    if ( start == null ) {
      for ( int i = 0; i < styles.length; i++ ) {
        try {
          start = DateFormat.getDateInstance( styles[i] ).parse ( s );
          break;
        } catch ( Exception e ) {}
        try {
          start = DateFormat.getTimeInstance( styles[i] ).parse ( s );
          break;
        } catch ( Exception e ) {}
      }
    }

    if ( start == null ) {
      logger.error ( "TemporalTrack Failed to parse datetime: " + s );
      
    }
  }

  public void setProject ( Project p ) {
    project = p;
  }

  void calculateStartEndTimes ( java.sql.Timestamp ts, Calendar beginning, Calendar end ) {
    Calendar trackStart = Calendar.getInstance();
    trackStart.setTime ( start );
    Calendar submissionTime = Calendar.getInstance();
    submissionTime.setTime ( ts );
    
    // Figure out what the starting point should be
    // duration is in hours
    long d = (long) ( duration * 3600000 ); // 60 minutes * 60 seconds * 1000 millis
    logger.debug ( "Duration is: " + d + " seconds" );
    // Compute the offset from 0
    long first = trackStart.getTimeInMillis() % d;

    long t = first + d * ( ( submissionTime.getTimeInMillis() - first ) / d );

    beginning.setTimeInMillis ( t );
    end.setTimeInMillis ( t + d );
    logger.debug ( "Time stamp is: " + ts );
    logger.debug ( "Beginning is: " + beginning.getTime() );
    logger.debug ( "End is: " + end.getTime() );

    logger.debug ( project.getTitle() + ": Duration in millis: " + d
                   + " first: " + first
                   + " submission: " + beginning.getTimeInMillis() );
  }


  public long getTrackId ( java.sql.Timestamp ts ) {
    long trackId = 0;
    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    try {
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
      TrackFinderBase trackFinder = new TrackFinderBase ( session );

      Calendar submissionTrackStart = Calendar.getInstance();
      Calendar submissionTrackEnd = Calendar.getInstance();
      calculateStartEndTimes ( ts, submissionTrackStart, submissionTrackEnd );
      Calendar zero = Calendar.getInstance();
      zero.setTimeInMillis ( 0 );

      logger.debug ( project.getTitle() 
                     + ": zero [" + format.format ( zero.getTime() ) + "]" );
    
      // Find the proper track
      TrackResultSet rs = trackFinder.selectIntersectingResultSet ( name, ts, ts );
      TrackEntity trackEntity;
      if ( rs.hasNext() ) {
        trackEntity = rs.next();
        logger.debug ( project.getTitle() + ": Found track: " + trackEntity.getTrackId() );
      } else {
        // Didn't find the track, create a new one
        logger.debug ( project.getTitle() + ": Creating new track" );
        trackEntity = trackFinder.newInstance();
        trackEntity.setName ( name );
        trackEntity.setStartTime ( new java.sql.Timestamp ( submissionTrackStart.getTimeInMillis() ) );
        trackEntity.setEndTime ( new java.sql.Timestamp ( submissionTrackEnd.getTimeInMillis() ) );

        // Force a commit to create the TrackId, then re-query
        session.commit();
        rs = trackFinder.selectIntersectingResultSet ( name, ts, ts );      
        if ( rs.hasNext() ) {
          trackEntity = rs.next();
        } else {
          throw new Exception ( project.getTitle() + ": Failed to create new Track!!!" );
        }
        
        // Update next/last
        QueryParams params = new QueryParams();
        params.add ( name );
        params.add ( new TimestampMapper(), trackEntity.getStartTime() );
        rs = trackFinder.findResultSet ( "where name = ? and EndTime <= ? order by EndTime desc", params );
        if ( rs.hasNext() ) {
          TrackEntity lastTrack = rs.next();
          lastTrack.setNextTrackId ( trackEntity.getTrackId() );
          trackEntity.setLastTrackId ( lastTrack.getTrackId() );
          logger.debug ( project.getTitle() + ": setting last track to " + lastTrack.getTrackId() + " end time " + lastTrack.getEndTime() );
          // Need this commit, or Jaxor puts in a duplicate "new" row
          session.commit();
        }
        params = new QueryParams();
        params.add ( name );
        params.add ( new TimestampMapper(), trackEntity.getEndTime() );
        rs = trackFinder.findResultSet ( "where name = ? and StartTime >= ? order by StartTime asc", params );
        if ( rs.hasNext() ) {
          TrackEntity nextTrack = rs.next();
          nextTrack.setLastTrackId ( trackEntity.getTrackId() );
          trackEntity.setNextTrackId ( nextTrack.getTrackId() );
          logger.debug ( project.getTitle() + ": setting next track to " + nextTrack.getTrackId() + " start time " + nextTrack.getStartTime() );
          // Need this commit, or Jaxor puts in a duplicate "new" row
          session.commit();
        }
      }
      trackId = trackEntity.getTrackId().longValue();
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Failed to find track", e );
    }
    try {
      connection.close();
    } catch ( Exception e ) {}
    return trackId;
  }

    
  public boolean isValidTrack ( TrackEntity track ) {
    Calendar submissionTrackStart = Calendar.getInstance();
    Calendar submissionTrackEnd = Calendar.getInstance();
    
    
    // Figure out if the start time is in the track
    java.sql.Timestamp ts = track.getStartTime();
    calculateStartEndTimes ( ts, submissionTrackStart, submissionTrackEnd );

    logger.debug ( "Checking to see if expected start time [ " + submissionTrackStart.getTime() + " ] equals [ " + ts + " ]" );
    if ( 0 != submissionTrackStart.getTime().compareTo ( track.getStartTime() ) ) {
      return false;
    }
    ts = track.getEndTime();
    if ( 0 != submissionTrackEnd.getTime().compareTo ( track.getEndTime() ) ) {
      return false;
    }
    return true;
  }


  public boolean placeSubmission ( long submissionId ) {
    /* Find or create the proper place for this submission, updating
       it's TrackId accordingly */
    logger.info ( project.getTitle() + ": placeSubmission" );

    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );

    try {
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
      SubmissionEntity submission = submissionFinder.selectBySubmissionId ( new Long ( submissionId ) );
      long trackId = getTrackId ( submission.getTimeStamp() );
      logger.debug ( project.getTitle() + ": Setting submission TrackId " + trackId );
      submission.setTrackId ( new Long ( trackId ) );
      session.commit();
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Error getting placing Submission", e );
    } finally {
      try {
        connection.close();
      } catch ( Exception e ) {}
    }
    return true;
  }

  public String toString () {
    StringBuffer buffer = new StringBuffer();
    buffer.append ( "TemporalTrack: " + name + " " + start + " " + duration );
    return buffer.toString();
  }
}


