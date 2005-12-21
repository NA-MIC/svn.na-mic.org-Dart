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
      
      int TotalErrors = 0;
      int TotalWarnings = 0;

      ResultFinderBase resultFinder = new ResultFinderBase ( session );
      
      // Walk the all the sub-tests
      // Build tests are of the form Build.StageX.Error, etc.
      TestIterator stages = build.selectChildren().iterator();
      while ( stages.hasNext() ) {
        TestEntity stage = stages.next();
        logger.debug ( "processing stage " + stage.getQualifiedName() );
        TestIterator children = stage.selectChildren().iterator();
        int Errors = 0;
        int Warnings = 0;
        while ( children.hasNext() ) {
          TestEntity child = children.next();
          logger.debug ( "processing test " + child.getQualifiedName() );
          if ( child.getQualifiedName().matches ( ".*Error.*" ) ) {
            logger.info ( project.getTitle() + ": Found error: " + child.getQualifiedName() );
            Errors++;
          }
          if ( child.getQualifiedName().matches ( ".*Warning.*" ) ) {
            logger.info ( project.getTitle() + ": Found warning: " + child.getQualifiedName() );
            Warnings++;
          }
        }
        set_result( resultFinder, stage, "ErrorCount", Errors );
        set_result( resultFinder, stage, "WarningCount", Warnings );
        TotalErrors += Errors;
        TotalWarnings += Warnings;
      }

      set_result( resultFinder, build, "ErrorCount", TotalErrors );
      set_result( resultFinder, build, "WarningCount", TotalWarnings );
      
      session.commit();
      session.flush();
    } catch ( Exception e ) {
      // rethrow
      throw e;
    } finally {
      connection.close();
    }

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
