package dart.server.listener;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.*;


import net.sourceforge.jaxor.JaxorContextImpl;

import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;

import dart.server.Project;
import dart.server.MessengerManager;
import dart.server.event.BuildEvent;
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
import dart.server.wrap.*;
import dart.server.messenger.Messenger;

public class SubmissionErrorsListener extends Listener {
  static Logger logger = Logger.getLogger ( SubmissionErrorsListener.class );

  public void trigger ( Project project, BuildEvent event ) throws Exception {
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

    try {
      // Check the submission for build errors (use a separate listener
      // for test failures)
      SubmissionEntity submission = submissionFinder.selectBySubmissionId( new Long(event.getSubmissionId()) );

      if (submission != null) {
        
        // Check to see if the submission matches the requested Track / Site / BuildName
        Pattern[] trackPatterns = project.generatePatterns ( properties.getProperty( "TrackPattern", "" ).split ( "," ) );
        Pattern[] sitePatterns = project.generatePatterns ( properties.getProperty( "SitePattern", "" ).split ( "," ) );
        Pattern[] buildNamePatterns = project.generatePatterns ( properties.getProperty( "BuildNamePattern", "" ).split ( "," ) );

        TrackEntity track = submission.getTrackEntity();
        if ( track != null && !project.matches ( track.getName(), trackPatterns ) ) {
          logger.debug ( "Didn't match Track" );
          return;
        }
        ClientEntity client = submission.getClientEntity();
        if ( client != null && !project.matches ( client.getSite(), sitePatterns ) ) {
          logger.debug ( "Didn't match Site" );
          return;
        }
        if ( !project.matches ( client.getBuildName(), buildNamePatterns ) ) {
          logger.debug ( "Didn't match BuildName" );
          return;
        }



        // check if there were any build errors
        if (submission.getErrorCount().longValue() > 0) {
          // Identify users who could have cause the errors.  Use a set
          // so that an author will be notified only once per submission.
          //
          HashSet<String> authorList = new HashSet<String>();

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
          HashSet<String> emailList = new HashSet<String>();

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

          // add any default email addresses
          HashSet<String> defaultContactList = new HashSet<String>();
          if (properties.containsKey("DefaultContactList")) {
            String[] defaultList
              = properties.getProperty("DefaultContactList").split(",");
            for (int i=0; i < defaultList.length; ++i) {
              defaultContactList.add(defaultList[i]);
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

          String url = "http://" + project.getServer().getServerName() + cp + "/Dashboard/Submission?submissionid=" + submission.getSubmissionId();
        
          // Build the content of the message
          String content = new String();
          content = "A submission to the Dart server for project \""
            + project.getTitle()
            + "\" has build errors. You have been identified as one of the authors who have checked in changes that are part of this submission or you are listed in the default contact list.  Details on the submission can be found at " + url + "\n\n";

          // Put the information of submission in the message
          content = content
            + "Project: " + project.getTitle() + "\n"
            + "Site: " + submission.getSite() + "\n"
            + "BuildName: " + submission.getBuildName() + "\n"
            + "Type: " + submission.getType() + "\n"
            + "Errors: " + submission.getErrorCount() + "\n"
            + "Warnings: " + submission.getWarningCount() + "\n"
            + "\n\n";
        
          // Find the first error for each stage of the build and report
          // it in the message 
          TestList firstErrorsPerStage
            = submission.selectTestListLike(".Build.Stage%.Error");
          TestIterator errorIt = firstErrorsPerStage.iterator();

          while(errorIt.hasNext()) {
            TestEntity error = errorIt.next();
            TestEntity parent = error.selectParent();
          
            content = content
              + "First error for stage "
              + parent.getResultValue("StageName", "(Unkown)")
              + ": \n"
              + "File: " + error.getResultValue("SourceFile", "(Unknown)")
              + "\n"
              + "Line: " + error.getResultValue("SourceLineNumber", "(Unknown)")
              + "\n"
              + error.getResultValue("PreContext", "")
              + "\n"
              + error.getResultValue("Text", "")
              + "\n"
              + error.getResultValue("PostContext", "")
              + "\n\n";
          }

          content = content
            + "- Dart server on " + project.getServer().getServerName();
        
        
          // Send the message by the mechanism specified
          //
          //
          try {
            logger.debug ( project.getTitle() + ": sending email to: " + emailList + " and " + defaultContactList + " with subject: " + subject );
            messenger.send(emailList, defaultContactList, subject, content);
          } catch (Exception e) {
            logger.error("Error sending notification: " + e);
          }
        }
      }
    } catch (Exception e) {
    } finally {
      // close the connection to the database
      try {
        logger.debug("Closing connection.");
        // connection.close();
        project.closeConnection ( connection );
      } catch (Exception e) {}
      try {
        logger.debug("Closing connection.");
        // serverConnection.close();
        project.getServer().closeConnection ( serverConnection );
      } catch (Exception e) {}
    }
  }

}

