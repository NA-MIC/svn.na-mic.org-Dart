package dart.server.listener;

import java.sql.Connection;
import java.util.*;

import org.apache.log4j.Logger;
import net.sourceforge.jaxor.JaxorContextImpl;
import net.sourceforge.jaxor.EntityNotFoundException;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;

import dart.server.Project;
import dart.server.MessengerManager;
import dart.server.messenger.Messenger;
import dart.server.event.MissingSubmissionEvent;

import dart.server.wrap.UserEntity;
import dart.server.wrap.UserIterator;
import dart.server.wrap.UserList;
import dart.server.wrap.UserFinderBase;
import dart.server.wrap.ClientEntity;
import dart.server.wrap.ClientIterator;
import dart.server.wrap.ClientList;
import dart.server.wrap.ClientFinderBase;
import dart.server.wrap.ClientPropertyFinderBase;


public class MissingSubmissionListener extends Listener {
  static Logger logger = Logger.getLogger ( MissingSubmissionListener.class );

  public void trigger ( Project project, MissingSubmissionEvent event ) throws Exception {

    // Get the messenger for this listener
    MessengerManager messengerManager = project.getMessengerManager();
    Messenger messenger
      = messengerManager.getMessenger(properties.getProperty("Messenger"));
    logger.info("Retrieved messenger " + properties.getProperty("Messenger") + "(" + messenger + ")");
    if (messenger == null) {
      logger.warn("No messenger specified.");
      return;
    }

    // Get the userids and clientids for the message
    List clientIds = event.getClientIds();
    Set  userIds   = event.getUserIds();

    // connect to the project database for clients
    Connection connection = project.getConnection();
    JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
    ClientFinderBase clientFinder = new ClientFinderBase( dbSession );
    ClientPropertyFinderBase clientPropertyFinder
      = new ClientPropertyFinderBase( dbSession );
    
    // connect to the server database for users
    Connection serverConnection = project.getServer().getConnection();
    JaxorContextImpl serverDBSession = new JaxorContextImpl ( serverConnection );
    UserFinderBase userFinder = new UserFinderBase( serverDBSession );

    // Construct the list of people to notify
    HashSet emailList = new HashSet();

    Iterator uit = userIds.iterator();
    Long userId;
    UserEntity user = null;
    while (uit.hasNext()) {
      userId = (Long) uit.next();
      
      try {
        user = userFinder.selectByUserId( userId );
        emailList.add( user.getEmail() );
      } catch ( EntityNotFoundException notfound ) {
        logger.info("User not found. UserId = " + userId);
      } catch (net.sourceforge.jaxor.util.SystemException exc) {
        logger.info("User not found. UserId = " + userId);
      }
      
    }

    // Build the url to put in the message
    HttpServer httpServer = project.getHttpServer();
    HttpContext httpContext = httpServer.getContext("/" + project.getTitle() + "/*");
    String cp = httpContext.getContextPath();

    String url = "http://" + java.net.InetAddress.getLocalHost().getCanonicalHostName() + ":" + project.getServer().getHttpPort() + cp + "/Dashboard/Dashboard?trackid=" + event.getTrackId();

    // Construct the content of the message
    String content = new String();
    content = "The following clients have not submitted to the Dart server for project \""
      + project.getTitle()
      + "\" on track \"" + event.getTrackName() + "\" within the alloted time:\n\n";
    Iterator cit = clientIds.iterator();
    Long clientId;
    ClientEntity client = null;
    while (cit.hasNext()) {
      clientId = (Long) cit.next();

      try {
        client = clientFinder.selectByClientId( clientId );
        content = content + "\t\t" + client.getSite() + " - "
          + client.getBuildName() + "\n";
      } catch ( EntityNotFoundException notfound ) {
        logger.warn("Client not found. ClientId = " + clientId);
      }
    }
    
    content = content + "\n\n"
      + "You are listed as a maintainer of one of these clients.  You may want"
      + " to check client. Details on the current dashboard are at " + url;
    
    // Build the subject of the message
    String subject = "Dart(" + project.getTitle() + ") - Expected submissions missing";

    // Send the message by the mechanism specified
    //
    try {
      messenger.send(emailList, subject, content);
    } catch (Exception e) {
      logger.error("Error sending notification: " + e);
    }
    
    // close the connections to the databases
    try { connection.close(); } catch ( Exception e2 ) { }
    try { serverConnection.close(); } catch ( Exception e2 ) { }
  }
}
