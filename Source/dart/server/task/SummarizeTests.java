package dart.server.task;

import java.io.File;
import java.io.*;
import java.sql.*;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

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

public class SummarizeTests implements Task {
  static Logger logger = Logger.getLogger ( SummarizeTests.class );   

  public HashMap getTestHashMap ( SubmissionEntity submission, boolean zero ) {
    HashMap map = new HashMap();
    // Find all the tests, add to map
    TestList tests = submission.getTestList();
    TestIterator testIterator = tests.iterator();
    while ( testIterator.hasNext() ) {
      TestEntity test = testIterator.next();
      if ( zero ) {
        test.setPassedSubTests ( new Integer ( 0 ) );
        test.setFailedSubTests ( new Integer ( 0 ) );
        test.setNotRunSubTests ( new Integer ( 0 ) );
      }
      map.put ( test.getQualifiedName(), test );
      logger.debug ( "Adding test " + test.getQualifiedName() );
    }
    return map;
  }

  public void execute ( Project project, Properties properties ) throws Exception {
  
    // Summarize the tests for a given submission
    String SubmissionId = properties.getProperty ( "SubmissionId" );

    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
    TestFinderBase testFinder = new TestFinderBase ( session );
    SubmissionEntity submission = submissionFinder.selectBySubmissionId ( new Long ( SubmissionId ) );

    try {    
      HashMap map;
      map = getTestHashMap ( submission, false );
      
      // Create any parent tests
      HashMap newParents = new HashMap();
      Iterator i;
      i = map.values().iterator();
      while ( i.hasNext() ) {
        TestEntity test = (TestEntity) i.next();
        String[] components = test.getQualifiedName().split ( "\\." );
        String parent = "";
        for ( int c = 1; c < components.length; c++ ) {
          TestEntity parentTest = (TestEntity) map.get ( parent );
          if ( parentTest == null ) {
            if ( !newParents.containsKey ( parent ) ) {
              // Create the (minimal) ParentTest
              logger.debug ( "Creating parent Test: \"" + parent + "\"" );
              parentTest = testFinder.newInstance();
              parentTest.setQualifiedName ( parent );
              parentTest.setName ( components[c-1] );
              // Status is set to "m" for meta test, it's just used in
              // the roll up, not actually counted towards Passed,
              // Failed, and NotRun
              parentTest.setStatus ( "m" );
              parentTest.setSubmissionId ( submission.getSubmissionId() );
              newParents.put ( parentTest.getQualifiedName(), parentTest );
            }
          }
          parent = parent + "." + components[c];
        }
      }
      
      // Write them all to the database, then refetch
      session.commit();
      session.flush();
      
      
      /* For some unknown reason, Jaxor holds on to the TestList fetched
         from the Submission object.  To work around, use the TestFinder
         directly. */
      map = new HashMap();
      TestList tests = testFinder.selectBySubmissionIdList ( new Long ( SubmissionId ) );
      TestIterator testIterator = tests.iterator();
      while ( testIterator.hasNext() ) {
        TestEntity test = testIterator.next();
        test.setPassedSubTests ( new Integer ( 0 ) );
        test.setFailedSubTests ( new Integer ( 0 ) );
        test.setNotRunSubTests ( new Integer ( 0 ) );
        map.put ( test.getQualifiedName(), test );
        logger.debug ( "Adding test " + test.getQualifiedName() );
      }
      
      i = map.values().iterator();
      while ( i.hasNext() ) {
        TestEntity test = (TestEntity) i.next();
        logger.debug ( project.getTitle() + ": Second fetch for SubmissionId: " + SubmissionId + " found: \"" + test.getQualifiedName() + "\"" );
      }
      
      // Loop over all the tests, rolling up totals and creating hierarchy
      i = map.values().iterator();
      while ( i.hasNext() ) {
        TestEntity test = (TestEntity) i.next();
        // Update the parents
        String[] components = test.getQualifiedName().split ( "\\." );
        if ( components.length < 1 ) {
          logger.error ( "Skipping test: " + test.getName() );
          continue;
        }
        String parent = "";
        String status = test.getStatus();
        logger.debug ( project.getTitle() + ": Processing " + test.getQualifiedName() );
        for ( int t = 1; t < components.length; t++ ) {
          // Process parent, then update, ignores test name
          logger.debug ( project.getTitle() + ": Parent " + parent );
          TestEntity parentTest = (TestEntity) map.get ( parent );
          if ( parentTest == null ) {
            logger.error ( "Did not find parent: \"" + parent + "\"" );
            throw new Exception ( "Did not find parent: " + parent );
          }
          test.setParentTestId ( parentTest.getTestId() );
          if ( status.equals ( "p" ) ) {
            Integer p = parentTest.getPassedSubTests();
            p = new Integer ( p.intValue() + 1 );
            parentTest.setPassedSubTests ( p );
          } else if ( status.equals ( "f" ) ) {
            Integer p = parentTest.getFailedSubTests();
            p = new Integer ( p.intValue() + 1 );
            parentTest.setFailedSubTests ( p );
          } else if ( status.equals ( "n" ) ) {
            Integer p = parentTest.getNotRunSubTests();
            p = new Integer ( p.intValue() + 1 );
            parentTest.setNotRunSubTests ( p );
          }
          parent = parent + "." + components[t];
        }
      }
      session.commit();
    } catch ( Exception e ) {
      throw e;
    } finally {
      connection.close();
    }
  }
}
