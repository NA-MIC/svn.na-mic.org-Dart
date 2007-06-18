package dart.server.task;

import java.io.File;
import java.io.*;
import java.net.URL;
import java.text.*;
import java.util.Properties;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import net.sourceforge.jaxor.*;
import java.util.zip.GZIPInputStream;
import java.sql.*;
import dart.*;
import dart.server.*;
import dart.server.servlet.*;
import dart.server.wrap.*;
import dart.server.wrapimpl.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.*;
import freemarker.ext.beans.*;

import org.apache.log4j.Logger;

import dart.server.Project;
import dart.server.TestProcessor;

public class ArchiveTask implements Task {
  static Logger logger = Logger.getLogger ( ArchiveTask.class );   
  int MaxDirectorySizeMB = 700;
  String FileNamePattern = "Archive-%L-%P-%S-%B-%T-%D.xml.gz";
  String ArchiverList = "";
  int ArchiveLevel = 0;
  long ArchiveSizeInK = 0;
  Connection connection;
  JaxorContextImpl session;
  QueryParams q = null;
  Project project = null;
  String TemplateName = "ArchiveSumbission.xml";
  File ArchiveDirectory = null;  
  File Working = null;  
  File Temporary = null;  
  
  private void archiveSubmission ( SubmissionEntity submission ) throws Exception {
    // Load the Freemarker template

    File TempFile = File.createTempFile ( "ArchiveSubmission", ".xml.gz", Temporary );

    // Setup the map of information that will be passed to the
    // template engine
    logger.debug ( "Starting to archive submission: " + submission.getSubmissionId() );
    HashMap root = new HashMap();
    root.put ( "fetchdata", new FetchData ( project ) );
    root.put ( "base64encode", new Base64Encode () );
    root.put ( "submission", submission );

    Configuration cfg = new Configuration();
    File resourcesDirectory = new File(project.getBaseDirectory(),"Templates");
    cfg.setDirectoryForTemplateLoading( resourcesDirectory );

    Template template = cfg.getTemplate ( TemplateName );
    Writer out = new BufferedWriter ( new OutputStreamWriter ( new GZIPOutputStream ( new FileOutputStream(TempFile)) ) );
    template.process ( root, out );
    // Make sure everything is written out, and closed down
    out.close();

    // Move the temp file to the archive directory
    String ArchiveFileName = FileNamePattern;

    /*    
      FileNamePattern specifies how the file will be constructed
      %L is replaced by ArchiveLevel
      %P is replaced by the project name
      %S is replaced by the Site name
      %B is replaced by the BuildName
      %T is replaced by the TrackName
      %D is replaced by the DateTimeStamp
      %N is replaced by the current time "now" in UTC
    */
    ArchiveFileName = ArchiveFileName.replaceAll ( "%L", Integer.toString ( ArchiveLevel ) );
    ArchiveFileName = ArchiveFileName.replaceAll ( "%P", project.getTitle() );
    ArchiveFileName = ArchiveFileName.replaceAll ( "%S", submission.getSite() );
    ArchiveFileName = ArchiveFileName.replaceAll ( "%B", submission.getBuildName() );
    ArchiveFileName = ArchiveFileName.replaceAll ( "%T", submission.getTrackEntity().getName() );
    SimpleDateFormat format = new SimpleDateFormat ( Container.UTCFormat );
    String ts = format.format ( submission.getTimeStamp() ).toString();
    ArchiveFileName = ArchiveFileName.replaceAll ( "%D", ts );
    ts = format.format ( Calendar.getInstance().getTime() ).toString();
    ArchiveFileName = ArchiveFileName.replaceAll ( "%N", ts );
    // Make sure we don't have any bad characters
    ArchiveFileName = Container.generateSafeFileName ( ArchiveFileName );

    // put the temp in the Working directory if it's not full
    int l = (int) (TempFile.length() / 1000.0 );
    if ( ( l + ArchiveSizeInK) > MaxDirectorySizeMB*1000 ) {
      // Rename the Working directory
      format = new SimpleDateFormat ( Container.UTCFormat );
      ts = format.format ( Calendar.getInstance().getTime() ).toString();
      ts = Container.generateSafeFileName ( ts );
      File newArchive = new File ( ArchiveDirectory, ts );
      Working.renameTo ( newArchive );
      Working.mkdirs();
      ArchiveSizeInK = 0;
    }
    ArchiveSizeInK += l;
    File destination = new File ( Working, ArchiveFileName );
    // logger.debug ( "TempFile: " + TempFile.getPath() );
    // logger.debug ( "destination: " + destination.getPath() );
    logger.debug ( "Finished archiving submission" );
    TempFile.renameTo ( destination );
  }
  
  
  
  private void deleteTest ( TestEntity test, boolean deleteNonBulkData ) throws Exception {
    ResultIterator results = test.getResultList().iterator();
    // Delete the Result, if a blob, add a new task to delete
    while ( results.hasNext() ) {
      ResultEntity result = results.next();
      logger.debug ( "Deleting Result: " + result.getName() );
      if ( Project.isLargeDataType ( result.getType() ) ) {
        // Queue the task
        Properties prop;
        prop = new Properties();
        prop.setProperty ( "ResultValue", result.getValue() );
        prop.setProperty ( "RecordCompletedTask", "false" );
        project.queueTask ( "dart.server.task.DeleteDataTask", prop, 100 );
      }
      if ( deleteNonBulkData || Project.isLargeDataType ( result.getType() ) ) {
        logger.debug ( "Doing delete" );
        result.delete();
      }
    }
    test.getJaxorContext().commit();
  }
  

  private void synchronizedExecute ( Project p, Properties properties ) throws Exception {
    if ( properties == null ) {
      logger.warn ( "Null properties" );
    }
    int SubmissionsToArchive = -1;
    int SubmissionsArchived = 0;
    SubmissionsToArchive = Integer.parseInt ( properties.getProperty ( "SubmissionsToArchive", "-1" ) );
    project = p;
    logger.info ( project.getTitle() + ": Starting Archive" );
    
    SimpleDateFormat format = new SimpleDateFormat ( Container.UTCFormat );
    String DirectoryName = format.format ( Calendar.getInstance().getTime() ).toString();
    logger.debug ( "DirectoryName: " + DirectoryName );
    
    connection = project.getConnection();
    session = new JaxorContextImpl ( connection );
    SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
    TestFinderBase testFinder = new TestFinderBase ( session );
    
    try {
      // Figure out what we need to do
      String[] Archivers = properties.getProperty( "ArchiverList", "" ).split ( "," );
      for ( int ArchiverIdx = 0; ArchiverIdx < Archivers.length; ArchiverIdx++ ) {
        String Archiver = Archivers[ArchiverIdx];
        logger.debug ( "Starting Archiver: " + Archiver );

        FileNamePattern = properties.getProperty ( "Archiver." + Archiver + ".FileNamePattern", "Archive-%P-%S-%B-%T-%D.xml.gz" );
        TemplateName = properties.getProperty ( "Archiver." + Archiver + ".Template", "ArchiveSubmission.xml" );

        String ArchiveBy = properties.getProperty ( "Archiver." + Archiver + ".ArchiveBy", "CreatedTimeStamp" );
        if ( !ArchiveBy.toLowerCase().equals ( "createdtimestamp" ) && !ArchiveBy.toLowerCase().equals ( "timestamp" ) ) {
          logger.error ( project.getTitle() + ": ArchiveBy must be CreatedTimeStamp or TimeStamp, skipping this Archiver" );
          continue;
        }

        String dir = properties.getProperty ( "Archiver." + Archiver + ".ArchiveDirectory", null );
        if ( dir == null || dir.equals ( "" ) ) {
          ArchiveDirectory = p.getArchiveDirectory();
        } else {
          ArchiveDirectory = new File ( dir );
        }

        MaxDirectorySizeMB = Integer.parseInt ( properties.getProperty ( "Archiver." + Archiver + ".MaxDirectorySizeMB", "700" ) );
    
        if ( !ArchiveDirectory.exists() && !ArchiveDirectory.mkdirs() ) {
          logger.error ( "Failed to make directory: " + ArchiveDirectory.getPath() );
          throw new Exception ( "Failed to make directory: " + ArchiveDirectory.getPath() );
        }
        // Find Archive size
        Working = new File ( ArchiveDirectory, "Working" );
        Working.mkdirs();
        Temporary = new File ( ArchiveDirectory, "Temporary" );
        Temporary.mkdirs();
        File[] children = Working.listFiles();
        ArchiveSizeInK = 0;
        for ( int i = 0; i < children.length; i++ ) {
          ArchiveSizeInK += (int) ( children[i].length() / ( 1000.0 ) );
        }
        logger.debug ( "Archive: " + Archiver + " ArchiveSizeInK: " + ArchiveSizeInK );
        String[] MatchTrack = properties.getProperty ( "Archiver." + Archiver + ".MatchTrack", ".*" ).split ( "," );
        String[] MatchTest = properties.getProperty ( "Archiver." + Archiver + ".MatchTest", ".*" ).split ( "," );
        String[] MatchSite = properties.getProperty ( "Archiver." + Archiver + ".MatchSite", ".*" ).split ( "," );
        String[] MatchBuildName = properties.getProperty ( "Archiver." + Archiver + ".MatchBuildName", ".*" ).split ( "," );
        String[] MatchRemove = properties.getProperty ( "Archiver." + Archiver + ".Remove", ".*" ).split ( "," );
        
        Pattern[] TrackPatterns = project.generatePatterns ( MatchTrack );
        Pattern[] TestPatterns = project.generatePatterns ( MatchTest );
        Pattern[] SitePatterns = project.generatePatterns ( MatchSite );
        Pattern[] BuildNamePatterns = project.generatePatterns ( MatchBuildName );
        Pattern[] RemovePatterns = project.generatePatterns ( MatchRemove );
        
        float AgeInDays = Float.parseFloat ( properties.getProperty ( "Archiver." + Archiver + ".AgeInDays", "-1.0" ) );
        if ( AgeInDays < 0.0 ) { 
          logger.debug ( project.getTitle() + ": Archiver: " + Archiver + " has no AgeInDays" );
          continue;
        }
        ArchiveLevel = Integer.parseInt ( properties.getProperty ( "Archiver." + Archiver + ".ArchiveLevel", "0" ) );
        boolean WriteArchive = Boolean.valueOf ( properties.getProperty ( "Archiver." + Archiver + ".WriteArchive", "true" ) ).booleanValue();
        
        // Find the Submissions
        Calendar age = Calendar.getInstance();
        age.add ( Calendar.MINUTE, (int)(-AgeInDays * 24 * 60) );
        q = new QueryParams();
        q.add ( new java.sql.Timestamp ( age.getTimeInMillis() ) );
        q.add ( ArchiveLevel );

        logger.debug ( "Age Timestamp: " + new java.sql.Timestamp ( age.getTimeInMillis() ) );

        SubmissionResultSet submissions;
        submissions = submissionFinder.findResultSet ( "where " + ArchiveBy + " < ? and ArchiveLevel < ?", q );
        while ( submissions.hasNext() ) {
          if ( SubmissionsToArchive != -1 ) {
            if ( SubmissionsArchived >= SubmissionsToArchive ) {
              logger.debug ( "Completed archive of " + SubmissionsArchived + " Submissions, break" );
              break;
            }
          }
          
          SubmissionEntity submission = submissions.next();
          ClientEntity client = submission.getClientEntity();
          TrackEntity track = submission.getTrackEntity();
          logger.debug ( "Found submission: " + client.getSite() + " / " + client.getBuildName() + " @ " + submission.getTimeStamp() );
          logger.debug ( "CreatedTimeStamp: " + submission.getCreatedTimeStamp() );
          
          // Can we delete it?
          // Check Track
          if ( track == null || !project.matches ( track.getName(), TrackPatterns ) ) {
            logger.debug ( "Didn't match Track" );
            continue;
          }
          // Check Sites
          if ( !project.matches ( client.getSite(), SitePatterns ) ) {
            logger.debug ( "Didn't match Site" );
            continue;
          }
          // Check BuildName
          if ( !project.matches ( client.getBuildName(), BuildNamePatterns ) ) {
            logger.debug ( "Didn't match BuildName" );
            continue;
          }
          
          // Now Archive the Submission if not already done
          java.sql.Timestamp compareTS = submission.getCreatedTimeStamp();
          if ( submission.getArchivedTimeStamp() == null
               || submission.getArchivedTimeStamp().before ( submission.getCreatedTimeStamp() ) ) {
            if ( WriteArchive ) {
              archiveSubmission ( submission );
            }
            submission.setArchivedTimeStamp ( new java.sql.Timestamp ( Calendar.getInstance().getTimeInMillis() ) );
            session.commit();
          }
          SubmissionsArchived++;
          logger.info ( project.getTitle() + ": Archiving " + SubmissionsArchived + " of " + SubmissionsToArchive );
          
          // Find all the tests
          TestResultSet tests = testFinder.selectBySubmissionIdResultSet ( submission.getSubmissionId() );
          while ( tests.hasNext() ) {
            TestEntity test = tests.next();
            logger.debug ( "Examining test: " + test.getQualifiedName() );
            if ( !project.matches ( test.getQualifiedName(), TestPatterns ) ) {
              logger.debug ( "Test didn't match" );
              continue;
            }
            /*
              ArchiveLevel indicates how much data to remove
              1 - remove least amount of data.  All bulk data (images, logs, etc...) are to be removed
              2 - remove all leaf tests and data, leaving only intermediate levels in the Test hierarchy
              3 - remove all non-root Tests, leaving rollup info at the root level of the Test hierarchy
              4 - remove all data and the Submission itself
            */
            if ( ArchiveLevel == 4 ) {
              deleteTest ( test, true );
              logger.debug ( "Level 4 Deleting Test: " + test.getQualifiedName() );
              test.delete();
              session.commit();
            } else if ( ArchiveLevel == 3 ) {
              TestEntity Parent = test.selectParent();
              if ( Parent != null && !Parent.getQualifiedName().equals ( "" ) ) {
                deleteTest ( test, true );
                logger.debug ( "Level 3 Deleting Test: " + test.getQualifiedName() );
                test.delete();
                session.commit();
              }
            } else if ( ArchiveLevel == 2 ) {
              if ( !test.getStatus().equals ( "m" ) ) {
                deleteTest ( test, true );
                logger.debug ( "Level 2 Deleting Test: " + test.getQualifiedName() );
                test.delete();
                session.commit();
              }
            } else if ( ArchiveLevel == 1 ) {              
              // Delete all large results, but not the test
              deleteTest ( test, false );
            }
          }
          tests.close();
          if ( ArchiveLevel == 4 ) {
            logger.debug ( "Deleting submission: " + client.getSite() + " / " + client.getBuildName() + " @ " + submission.getTimeStamp() );
            submission.delete();
          } else {
            submission.setArchiveLevel ( new Integer ( ArchiveLevel ) );
          }
          session.commit();
        }
        submissions.close();
      }
      session.commit();
    } catch ( Exception e ) {
      logger.error ( "Failed to Archive task", e );
      throw e;
    } finally {
      logger.debug("Closing connection.");
      project.closeConnection ( connection );
    }
  }
  
  public void execute ( Project project, Properties properties ) throws Exception {
    // Syncronize on the Project, i.e. only one ArchiveTask per project
    logger.debug ( "Synchronizing on project" );
    synchronized ( project.getLockObject ( this.getClass().toString() ) ) {
      logger.info ( "Lock acquiried, starting execute" );
      synchronizedExecute ( project, properties );
      logger.info ( "Finished execute, released lock" );
    }
  }
}
