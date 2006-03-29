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

public class SummarizeBuildTask implements Task {
  static Logger logger = Logger.getLogger ( SummarizeBuildTask.class );   
  public void execute ( Project project, Properties properties ) throws Exception {

    
    // Summarize the tests for a given submission
    String SubmissionId = properties.getProperty ( "SubmissionId" );
    Connection connection = project.getConnection();
    try {
      JaxorContextImpl session = new JaxorContextImpl ( connection );
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
      TestFinderBase testFinder = new TestFinderBase ( session );
      SubmissionEntity submission = submissionFinder.selectBySubmissionId ( new Long ( SubmissionId ) );
      
      TestEntity build = submission.selectTest ( ".Build" );
      if ( build == null ) {
        logger.debug ( project.getTitle() + ": No Build info found" );
        return;
      }

      logger.debug ( project.getTitle() + ": processing " + build.getQualifiedName() );
      
      ResultFinderBase resultFinder = new ResultFinderBase ( session );

      summarize_build( resultFinder, build );
      
      session.commit();
      session.flush();
    } catch ( Exception e ) {
      // rethrow
      throw e;
    } finally {
      connection.close();
    }

  }

  /** Count up the errors in this build.
   *
   * Every subtest with a name ending in "*Error*" counts as a direct
   * error for this build. It follows that tests with names that
   * actually end in "Error*" are not counted as "builds"; they are
   * just convenience tests to record specific errors in a
   * build. However, subtests that have other names are treated as a
   * subtree, and are processed recursively.
   *
   * The total number of direct errors for this build are summarized
   * in a result called "SelfErrorCount". That added to the counts of
   * the subtests gives a total error count for the tree, and is
   * stored in a result called "ErrorCount".
   *
   * Similarly for warnings.
   *
   * The return value of this function is a two-element array
   * containing the total error and warning counts for the tree rooted
   * at build.
   */
  private int[] summarize_build( ResultFinderBase resultFinder, TestEntity build ) {
    int TotalCounts[] = { 0, 0 };
    int SelfErrors = 0;
    int SelfWarnings = 0;
    float ElapsedTime = 0.0f;

    logger.debug ( "processing build " + build.getQualifiedName() );
    
    // Walk the all the sub-tests.
    TestIterator children = build.selectChildren().iterator();
    while ( children.hasNext() ) {
      TestEntity child = children.next();

      // If the name is "*.ErrorX" or "*.WarningX", then this test
      // represents an error or warning. Otherwise, it represents
      // another grouping that we should recurse into.
      if( child.getQualifiedName().matches( ".*Error[^.]*" ) ) {
        ++SelfErrors;
      } else if( child.getQualifiedName().matches( ".*Warning[^.]*" ) ) {
        ++SelfWarnings;
      } else {
        int[] counts = summarize_build( resultFinder, child );
        TotalCounts[0] += counts[0];
        TotalCounts[1] += counts[1];
      }

      if ( build.getQualifiedName().equals ( ".Build" ) ) {
        String t = child.getResultValue ( "ElapsedTime", "0.0" );
        ElapsedTime += Float.parseFloat ( t );
      }

    }

    TotalCounts[0] += SelfErrors;
    TotalCounts[1] += SelfWarnings;

    set_result( resultFinder, build, "ErrorCount", TotalCounts[0] );
    set_result( resultFinder, build, "WarningCount", TotalCounts[1] );
    set_result( resultFinder, build, "SelfErrorCount", SelfErrors );
    set_result( resultFinder, build, "SelfWarningCount", SelfWarnings );
    if ( build.getQualifiedName().equals ( ".Build" ) ) {
      build.setResult ( "ElapsedTime", "numeric/float", "" + ElapsedTime );
    }

    return TotalCounts;
  }


  /**
     Set a summary value on a test, if it isn't there already.
   */
  private void set_result ( ResultFinderBase resultFinder, TestEntity test, String name, int value ) {
    ResultList results = test.selectResult ( name );
    if ( results.toArray().length == 0 ) {
      logger.debug ( "Setting " + test.getQualifiedName() + "." + name + " to " + value );
      ResultEntity result = resultFinder.newInstance();
      result.setTestId ( test.getTestId() );
      result.setType ( "numeric/integer" );
      result.setName ( name );
      result.setValue ( Integer.toString ( value ) );
    } else if ( results.toArray().length == 1 ) {
      ResultEntity result = results.toArray()[0];
      String valueStr = Integer.toString(value);
      if( ! result.getValue().equals( valueStr ) ) {
        logger.debug ( "Updating " + test.getQualifiedName() + "." + name + " to " + valueStr );
        result.setValue ( valueStr );
      } else {
        logger.debug ( test.getQualifiedName() + "." + name + " is already " + valueStr );
      }
    } else {
      logger.error ( "Already multiple " + test.getQualifiedName() + "." + name );
    }
  }

}
