package dart.server.task;

import java.sql.Connection;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.*;
import java.text.*;

import net.sourceforge.jaxor.JaxorContextImpl;
import org.apache.log4j.Logger;

import dart.server.Project;
import dart.server.event.MissingSubmissionEvent;
import dart.server.wrap.SubmissionEntity;
import dart.server.wrap.SubmissionList;
import dart.server.wrap.SubmissionFinderBase;
import dart.server.wrap.*;
import dart.server.wrap.ClientIterator;
import dart.server.wrap.ClientList;
import dart.server.wrap.ClientFinderBase;
import dart.server.wrap.ClientPropertyEntity;
import dart.server.wrap.ClientPropertyFinderBase;
import dart.server.wrap.ClientPropertyIterator;
import dart.server.wrap.ClientPropertyList;

public class MissingSubmissionTask implements Task {
  static Logger logger = Logger.getLogger ( MissingSubmissionTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {
    boolean missing = false;
    ArrayList clientIds = new ArrayList();
    HashSet userIds = new HashSet();
    
    // Determine the track to search
    java.util.Date date = new java.util.Date();
    java.sql.Timestamp timeStamp = new java.sql.Timestamp ( date.getTime() );

    String trackName = properties.getProperty ( "Track" );
    Long trackId
      = new Long(project.getTrackManager().getTrackId( timeStamp, trackName ));

    // Client property names
    //
    // If a client is expected, it will have client property called
    //
    //       Expected.<TrackName>
    //
    // whose value is "true".
    //
    // If a user has been identified as a person to notify if a
    // submission is missing, the client will have a client property
    // called
    //
    //       Expected.<TrackName>.Notify.UserId
    //
    // which will have a value of a long.
    //
    String expectedSubmissionKey = "Expected." + trackName;
    String expectedSubmissionUserKey
      = expectedSubmissionKey + ".Notify.UserId";

    
    // connect to the project database for clients and submissions
    Connection connection = project.getConnection();
    JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
    ClientFinderBase clientFinder = new ClientFinderBase( dbSession );
    ClientPropertyFinderBase clientPropertyFinder
      = new ClientPropertyFinderBase( dbSession );
    SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( dbSession );
    
    // connect to the server database for users
    Connection serverConnection = project.getServer().getConnection();
    JaxorContextImpl serverDBSession = new JaxorContextImpl ( serverConnection );

    // Find the clients that are expected.  Search for clients that
    // have a ClientProperty named "Expected.<TrackName>" with value "true"
    ClientResultSet clientList
      = clientFinder.selectByClientPropertyResultSet ( expectedSubmissionKey, "true");

    while (clientList.hasNext()) {
      // There exist clients that are expected.  Check if any of these
      // clients have a submission in the specifed track.  If not,
      // cache the client id for the listener
      ClientEntity client = clientList.next();
      Long clientId = client.getClientId();
        
      // query for submissions on this track and from this client
      SubmissionList submissionList
        = submissionFinder.selectByClientIdAndTrackIdList(clientId, trackId);

      if (submissionList.size() == 0) {
        missing = true;
          
        // cache the client id for the event
        clientIds.add( clientId );

        // determine who needs to be notified
        ClientPropertyEntity cp = null;
        ClientPropertyResultSet who
          = clientPropertyFinder.selectByClientIdNameResultSet(clientId,
                                                          expectedSubmissionUserKey); 

        while (who.hasNext()) {
          // parse userids as longs from the properties
          cp = who.next();
          try {
            Long userid = new Long(Long.parseLong(cp.getValue()));
            userIds.add(userid);
          } catch (NumberFormatException exc) {
            // value is not a long, skip it
          }
        }
        who.close();
      }
    }
    clientList.close();

    // if something was missing, trigger a MissingSubmissionEvent
    if (missing) {
      MissingSubmissionEvent event
        = new MissingSubmissionEvent (clientIds, userIds, trackName, trackId);
      project.getListenerManager().triggerEvent ( event );
    }

    // close the connections to the databases
    try {
      logger.debug("Closing connection.");
      // connection.close();
      project.closeConnection ( connection );
    } catch ( Exception e2 ) { }
    try {
      logger.debug("Closing connection.");
      // serverConnection.close();
      project.getServer().closeConnection ( serverConnection );
    } catch ( Exception e2 ) { }
  }      
}
