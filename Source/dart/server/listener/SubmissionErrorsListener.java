package dart.server.listener;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;


import net.sourceforge.jaxor.JaxorContextImpl;

import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;

import dart.server.Project;
import dart.server.MessengerManager;
import dart.server.event.SubmissionEvent;
import dart.server.wrap.SubmissionEntity;
import dart.server.wrap.SubmissionFinderBase;
import dart.server.wrap.TestEntity;
import dart.server.wrap.TestIterator;
import dart.server.wrap.TestList;
import dart.server.wrap.UserEntity;
import dart.server.wrap.UserFinderBase;
import dart.server.wrap.UserPropertyEntity;
import dart.server.wrap.UserPropertyFinderBase;
import dart.server.wrap.UserPropertyIterator;
import dart.server.wrap.UserPropertyList;
import dart.server.messenger.Messenger;

public class SubmissionErrorsListener extends Listener {
  static Logger logger = Logger.getLogger ( SubmissionErrorsListener.class );

  public void trigger ( Project project, SubmissionEvent event ) throws Exception {
    logger.info ( "Submission event for SubmissionId " + event.getSubmissionId() );

    // Get the messenger for this listener
    MessengerManager messengerManager = project.getMessengerManager();
    Messenger messenger
      = messengerManager.getMessenger(properties.getProperty("Messenger"));
    logger.info("Retrieved messenger " + properties.getProperty("Messenger") + "(" + messenger + ")");
    if (messenger == null) {
      logger.warn("No messenger specified.");
      return;
    }
        
    // connect to the project database for submissions and results
    Connection connection = project.getConnection();
    JaxorContextImpl dbSession = new JaxorContextImpl ( connection );
    SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( dbSession );

    // connect to the server database for users
    Connection serverConnection = project.getServer().getConnection();
    JaxorContextImpl serverDBSession = new JaxorContextImpl ( serverConnection );
    UserFinderBase userFinder = new UserFinderBase( serverDBSession );
    UserPropertyFinderBase userPropertyFinder
      = new UserPropertyFinderBase( serverDBSession );
    
    // Check the submission for build errors (use a separate listener
    // for test failures)
    SubmissionEntity submission = submissionFinder.selectBySubmissionId( new Long(event.getSubmissionId()) );

    if (submission != null) {
      // check if there were any build errors
      if (submission.getErrorCount().longValue() > 0) {
        // Identify users who could have cause the errors.  Use a set
        // so that an author will be notified only once per submission.
        //
        HashSet authorList = new HashSet();

        TestEntity update = submission.selectTest(".Update.Update");
        if (update != null) {
          TestList tlist = update.selectChildren();
          if (tlist.size() > 0) {
            TestIterator tit = tlist.iterator();
            TestEntity updatedFile = null;
            String author;
            while (tit.hasNext()) {
              updatedFile = tit.next();
              
              author = updatedFile.getResultValue("Author", "");
              if (!author.equals("")) {
                authorList.add(author);
              }
            }
          }
        }

        // convert the author list to of Dart user account names
        HashSet emailList = new HashSet();

        UserEntity user = null;
        UserPropertyEntity up = null;
        UserPropertyList uplist = null;

        String propertyName = project.getTitle() + ".RepositoryId";
        
        Iterator ait = authorList.iterator();
        while (ait.hasNext()) {
          String author = (String)ait.next();

          uplist = userPropertyFinder.selectByNameValueList( propertyName,
                                                             author );
          if (uplist.size() > 0) {
            // at least one user has this repository id registered for
            // this project.  get the email addresses for these users
            UserPropertyIterator upit = uplist.iterator();
            while (upit.hasNext()) {
              up = upit.next();

              user = userFinder.selectByUserId(up.getUserId());
              emailList.add(user.getEmail());
            }
          }
        }

        // Build the subject of the message
        String subject = "Dart(" + project.getTitle() + ") - "
                   + submission.getSite() + " - "
                   + submission.getBuildName() + " - "
                   + submission.getType() + " - "
                   + submission.getTimeStamp() + " - "
                   + submission.getErrorCount() + " errors";
        
        
        // Build the url to put in the message
        HttpServer httpServer = project.getHttpServer();
        HttpContext httpContext = httpServer.getContext("/" + project.getTitle() + "/*");
        String cp = httpContext.getContextPath();

        String url = "http://" + java.net.InetAddress.getLocalHost().getCanonicalHostName() + ":" + project.getServer().getHttpPort() + cp + "/Dashboard/Submission?submissionid=" + submission.getSubmissionId();
        
        // Build the content of the message
        String content = new String();
        content = "A submission to the Dart server for project \""
          + project.getTitle()
          + "\" has build errors. You have been identified as one of the authors who have checked in changes that are part of this submission.  Details on the submission can be found at " + url;

        // Send the message by the mechanism specified
        //
        //
        try {
          messenger.send(emailList, subject, content);
        } catch (Exception e) {
          logger.error("Error sending notification: " + e);
        }
      }
    }

    // close the connection to the database
    try { connection.close(); } catch (Exception e) {}
    try { serverConnection.close(); } catch (Exception e) {}
    
  }

}

