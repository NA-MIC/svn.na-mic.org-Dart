package dart.server;

import org.apache.log4j.*;
import dart.server.TestProxy;
import java.sql.*;
import java.util.*;
import java.text.*;
import java.io.File;
import java.util.zip.*;
import java.io.*;
import java.io.FileOutputStream;
import java.util.Iterator;

import net.sourceforge.jaxor.*;
import dart.server.wrap.*;
import dart.server.util.*;

/**
   Class to process a XML file
   The primary purpose of this class is to place data of any sort into
   the database.  This class must be used in conjunction with the
   Digestor and <code>TestProcessorRules.xml</code>.  The rules
   specify how to process the different XML file formats that Dart
   understands.  TestProxy classes are created and pushed
   onto the Digestor stack and handled by TestProcessor.
   @author Dan Blezek
   @version $Revision:$
   @see TestProxy
*/
public class TestProcessor {
  static Logger logger = Logger.getLogger ( TestProcessor.class );   
  Project project = null;

  SubmissionEntity submission = null;
  ClientEntity client = null;

  String BuildName = null;
  Timestamp timestamp = null;
  String Type = null;
  String Site = null;
  String Generator= null;
  HashMap repeated = new HashMap();
  boolean foundSubmission = false;
  int maxTests = -1;
  int testsProcessed = 0;

  boolean delayProcessing = false;
  Vector pending = new Vector();

  /**
     Constructor
     @param p Parent Project
  */
  public TestProcessor ( Project p ) {
    project = p;
    logger.debug ( "Creating TestProcessor for " + project.getTitle() );
  }

  /**
     Set a limit on the number of test to process
     @param t Maximum number of test to process
  */
  public void setMaxTests ( int t ) { maxTests = t; }

  /**
   * Change handling of tests to delay till end or not
   * @param d flag to set delayed processing
   */
  public void setDelayProcessing ( boolean d ) { delayProcessing = d; }
  public void setDelayProcessing ( String d ) { 
    logger.debug ( "setDelayProcessing to " + d );
    delayProcessing = Boolean.valueOf ( d ).booleanValue(); 
  }
  public void setDelayProcessingOn () { delayProcessing = true; }
  public void setDelayProcessingOff () { delayProcessing = false; }

  /**
     Set the Site name, called from Digestor
     @param name Name of the Site
  */
  public void setSite ( String name ) {
    Site = name;
    logger.debug ( "setSite: " + Site );
  }

  /**
     Set the BuildName name, called from Digestor
     Note: In bean jargon, the property BuildName must map to
     buildname and a method setBuildname.
     @param buildname Name of the BuildName
  */
  public void setBuildname ( String buildname ) {
    // Note! In bean jargon, the property BuildName must map to buildname and a method setBuildname... 
    BuildName = buildname;
    logger.debug ( "setBuildName: " + BuildName  );
  }

  /**
     Set the BuildStamp name, called from Digestor
     Note: In bean jargon, the property BuildStamp must map to
     buildstamp and a method setBuildstamp.
     @param buildstamp Name of the BuildStamp
  */
  public void setBuildstamp ( String buildstamp ) {
    logger.debug ( project.getTitle() + ": Setting buildstamp " + buildstamp );
    timestamp = new Timestamp ( project.parseBuildStamp ( buildstamp ).getTime() );
    if ( Type == null ) {
      Type = buildstamp.substring ( 14 );
    }
    logger.debug ( "setTimeStamp: " + timestamp );
    logger.debug ( "setType: " + Type );
  }

  /**
     Set the Generator name, called from Digestor.
     Note: In bean jargon, the property Generator must map to
     generator and a method setGenerator.
     @param generator Name of the Generator
  */
  public void setGenerator ( String generator ) {
    logger.debug ( project.getTitle() + ": Setting generator " + generator );

    Generator = generator;
  }
  
  /**
     Set the DateTimeStamp, called from Digestor
     @param stamp Name of the BuildStamp
  */
  public void setDateTimeStamp ( String stamp ) {
    logger.debug ( "Setting DateTimeStamp" );
    timestamp = new Timestamp ( project.parseBuildStamp ( stamp ).getTime() );
    if ( Type == null ) {
      Type = stamp.substring ( 14 );
    }
    logger.debug ( "setTimeStamp: " + timestamp );
    logger.debug ( "setType: " + Type );
  }

  /**
     Set the Track or type
     @param track
  */
  public void setTrack ( String t ) {
    Type = t;
  }


  /**
     Place a summarize job in the queue
     After the file is processed, the Project calls this method to put
     tasks in the queue.  These are defined in the RollupManager
     @see dart.server.task.SummarizeTests
     @see dart.server.task.PlaceSubmissionInTrackTask
     @see dart.server.task.SummarizeBuildTask
     @see dart.server.task.SummarizeCoverage
     @see dart.server.task.SummarizeDynamicAnalysis
  */
  public void queueSummary () {
    // Queue the report information
    if ( submission != null ) {
      Properties prop;
      Iterator rollup = project.getRollups().iterator();
      String type;
      int priority;
      while ( rollup.hasNext() ) {
        Object[] d = (Object[])rollup.next();
        type = (String)d[0];
        priority = Integer.parseInt ( (String)d[1] );
        prop = new Properties((Properties)d[2]);
        prop.setProperty ( "SubmissionId", submission.getSubmissionId().toString() );
        prop.setProperty ( "TrackName", submission.getType().toString() );
        logger.debug ( project.getTitle() + ": Queuing Rollup: " + type + " Priority: " + priority + " Properties: \n" + prop );
        project.queueTask ( type, prop, priority );
      }

      // increment the activity statistic
      project.setStatistic("LastActivity", new java.util.Date().toString());
    }
  }

  /**
     Passthrough, process the proxy
     @param proxy TestProxy to process
     @see dart.server.TestProcessor#processTestProxy
  */
  public void addChild ( TestProxy proxy ) {
    // Process

    if ( BuildName == null ) {
      delayProcessing = true;
    }

    if ( delayProcessing ) {
      pending.add ( proxy );
    } else {
      processTestProxy ( proxy );
    }
  }

  /**
     Process delayed TestProxies
  */
  public void processDelayed () {
    Iterator i = pending.iterator();
    while ( i.hasNext() ) {
      processTestProxy ( (TestProxy) i.next() );
    }
  }
    

  /**
     Process the TestProxy object
     Recursively process the TestProxy object, placing new tests in the
     database.  If maxTests is exceeded, ignore this test.  Duplicate Tests
     are ignored.
     @param proxy TestProxy to be processed
     @see TestProxy
  */
  public void processTestProxy ( TestProxy proxy ) {

    if ( testsProcessed > maxTests ) {
      logger.info ( project.getTitle() + ": Reached maximum tests: " + maxTests );
      return;
    }
    testsProcessed++;

    // Do children first
    if ( proxy.getChildren() != null ) {
      Iterator children = proxy.getChildren().iterator();
      while ( children.hasNext() ) {
        processTestProxy ( (TestProxy) children.next() );
      }
    }

    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    QueryParams q = null;

    logger.debug ( "Processing test: " + proxy.getQualifiedName() );

    try {
      if ( client == null ) {
        ClientFinderBase clientFinder = new ClientFinderBase ( session );
        q = new QueryParams ();
        q.add ( BuildName );
        q.add ( Site );
        try {
          logger.debug ( "Seaching for " + BuildName + " / " + Site );
          client = clientFinder.findUnique ( "where BuildName = ? and Site = ?", q, false );
        } catch ( EntityNotFoundException notfound ) {
          logger.debug ( "Creating new Client instance: " + BuildName + " / " + Site );
          client = clientFinder.newInstance();
          client.setBuildName ( BuildName );
          client.setSite ( Site );
          session.commit();
          client = clientFinder.findUnique ( "where BuildName = ? and Site = ?", q, false );
          logger.debug ( "New Client instance: " + client.getClientId() );
        }
      }
      if ( submission == null ) {
        SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
        q = new QueryParams();
        q.add ( client.getClientId() );
        q.add ( timestamp );
        q.add ( Type );
        try {
          submission = submissionFinder.findUnique ( "where ClientId = ? and Timestamp = ? and Type = ?", q, false );
        } catch ( EntityNotFoundException notfound ) {
          logger.debug ( "Creating new Submission instance: " + timestamp + " - " + Type );
          submission = submissionFinder.newInstance();
          submission.setTimeStamp ( timestamp );
          submission.setClientId ( client.getClientId() );
          submission.setType ( Type );
          submission.setGenerator( Generator );
          submission.setStatus ( "InProgress" );
          session.commit();
          submission = submissionFinder.findUnique ( "where ClientId = ? and Timestamp = ? and Type = ?", q, false );
          logger.debug ( "New Submission: " + submission.getSubmissionId() ); 
        }
      }
      TestFinderBase testFinder = new TestFinderBase ( session );
      TestEntity test;
      q = new QueryParams();
      q.add ( submission.getSubmissionId() );
      q.add ( proxy.getQualifiedName() );
      logger.debug ( "Testname: "+ proxy.getQualifiedName() );
      try {
        if ( proxy.isRepeated() ) {
          int id = 0;
          // Find and increment the test proxy name
          String Name = proxy.getQualifiedName();
          // See what's in the database
          boolean done = false;
          while ( !done ) {
            q = new QueryParams();
            q.add ( submission.getSubmissionId() );
            q.add ( proxy.getQualifiedName() );
            TestList l = testFinder.find ( "where SubmissionId = ? and QualifiedName = ?", q );
            if ( l.size() == 0 ) {
              done = true;
            } else {
              proxy.setQualifiedNameNoReplace ( Name + id );
              id++;
            }
          }
        } 
        q = new QueryParams();
        q.add ( submission.getSubmissionId() );
        q.add ( proxy.getQualifiedName() );
        test = testFinder.findUnique ( "where SubmissionId = ? and QualifiedName = ?", q, false );
        if ( proxy.allowDuplicates() == false ) {
          logger.debug ( "Duplicate Test in DB, ignoring. " + proxy.getQualifiedName());
          return;
        }
      } catch ( EntityNotFoundException notfound ) {
        test = testFinder.newInstance();
        test.setSubmissionId ( submission.getSubmissionId() );
        test.setQualifiedName ( proxy.getQualifiedName() );
        String[] n = proxy.getQualifiedName().split ( "\\." );
        test.setName ( n[n.length-1] );
        test.setStatus ( proxy.getStatus() );
          
        session.commit();
        test = testFinder.findUnique ( "where SubmissionId = ? and QualifiedName = ?", q, false );
      }

      // Update the submission with a new created time as "now"
      submission.setCreatedTimeStamp ( new java.sql.Timestamp ( java.util.Calendar.getInstance().getTimeInMillis() ) );

      // If the proxy has as status of failed, then it can overwrite
      // the previous test status (default value or separate submission)
      if (proxy.getStatus().equals("f")) {
        test.setStatus( proxy.getStatus() );
      }
                        
      
      ResultFinderBase resultFinder = new ResultFinderBase ( session );
      Iterator i = proxy.getResultMap().keySet().iterator();
      while ( i.hasNext() ) {
        String name = (String) i.next();
        String[] s = (String[]) proxy.getResultMap().get ( name );
        String type = s[0];
        String value = s[1];
        boolean compress = false;

        ResultEntity result;
        try {
          q = new QueryParams();
          q.add ( name );
          q.add ( test.getTestId() );
          result = resultFinder.findUnique ( "where Name = ? and TestId = ?", q, false );
        } catch ( EntityNotFoundException notfound ) {
          result = resultFinder.newInstance();
        }
        result.setName ( name );
        result.setType ( type );
        result.setTestId ( test.getTestId() );
        logger.debug ( "Set TestId: " + test.getTestId() );

        // Here put data somewhere if it's of type text/text
        if ( Project.isLargeDataType ( type ) ) {
          String suffix = ".txt.gz";
          byte[] bytes = value.getBytes();
          compress = true;
          if ( type.equals ( "text/xml" ) ) {
            suffix = ".xml.gz";
            compress = true;
          } else if ( type.equals ( "image/png" ) ) {
            suffix = ".png";
            compress = false;
            logger.debug ( project.getTitle() + ": Base64 decoding result" );
            bytes = Base64.decode ( bytes, 0, bytes.length );
          } else if ( type.equals ( "image/jpeg" ) ) {
            suffix = ".jpeg";
            compress = false;
            logger.debug ( project.getTitle() + ": Base64 decoding result" );
            bytes = Base64.decode ( bytes, 0, bytes.length );
          } else if ( type.equals ( "archive/zip" ) ) {
            suffix = ".zip";
            compress = false;
            logger.debug ( project.getTitle() + ": Base64 decoding archive/zip" );
            bytes = Base64.decode ( bytes, 0, bytes.length );
          }
          if ( compress ) {
            logger.debug ( project.getTitle() + ": compressing result" );
            ByteArrayInputStream in = new ByteArrayInputStream ( value.getBytes() );
            ByteArrayOutputStream bout = new ByteArrayOutputStream ();
            GZIPOutputStream out = new GZIPOutputStream ( bout );
            int count;
            byte[] buffer = new byte[1024];
            while ( (count = in.read ( buffer ) ) != -1 ) {
              out.write ( buffer, 0, count );
            }
            out.finish();
            bytes = bout.toByteArray();
          }

          File output = project.generateProjectRelativeFileForBinary ( bytes, suffix );
          File abs = new File ( project.getDataDirectory(), output.getPath() );
          abs.getParentFile().mkdirs();
          result.setValue ( output.getPath() );

          // If the file exists, we do not need to create again
          if ( !abs.exists() ) {
            logger.debug ( "Creating new file: " + abs );
            OutputStream out = new BufferedOutputStream ( new FileOutputStream ( abs ) );
            out.write ( bytes );
            out.flush();
          }
        } else {
          // Only take the first 2000 bytes
          if ( value.length() > 2000 ) {
            logger.warn ( project.getTitle() + ": Truncating Value > 2000 characters, " + test.getQualifiedName() + " / " + result.getName() );
            value = value.substring ( 0, 1999 );
          }
          result.setValue ( value );
        }
      }
      session.commit();
      project.incrementStatistic ( "TestsProcessed" );
    } catch ( Exception e ) {
      logger.error ( "Failed to process Test", e );
    } finally {
      try {
        logger.info("Closing connection.");
        project.closeConnection ( connection );
      } catch ( Exception e ) { }
    }
  }

  /**
     Set info from CruseControl
     We care about these tags:
       projectname: should be composed of Project:BuildName:Site:Track
       builddate: this will be parsed as the Timestamp and must be in UTC
  */
  public void setCruiseControlInfo ( String name, String value ) {
    logger.debug ( "setCruiseControlInfo: " + name + " = " + value );
    if ( name != null && name.equals ( "builddate" ) ) {
      // Set the date time stamp
      setBuildstamp ( value );
    }
  }
}


