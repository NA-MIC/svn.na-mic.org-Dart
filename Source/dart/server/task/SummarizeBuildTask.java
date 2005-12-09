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
      
      int Errors = 0;
      int Warnings = 0;
      
      // Walk the all the sub-tests
      TestIterator children = build.selectChildren().iterator();
      while ( children.hasNext() ) {
        TestEntity child = children.next();
        if ( child.getQualifiedName().matches ( ".*Error.*" ) ) {
          logger.info ( project.getTitle() + ": Found error: " + child.getQualifiedName() );
          Errors++;
        }
        if ( child.getQualifiedName().matches ( ".*Warning.*" ) ) {
          logger.info ( project.getTitle() + ": Found warning: " + child.getQualifiedName() );
          Warnings++;
        }
      }
      
      ResultFinderBase resultFinder = new ResultFinderBase ( session );
      ResultEntity result;
      // See if we have already done this and update
      ResultList results;
      results = build.selectResult ( "ErrorCount" );
      if ( results.toArray().length > 0 ) {
        result = results.toArray()[0];
      } else {
        result = resultFinder.newInstance();
        result.setTestId ( build.getTestId() );
        result.setType ( "numeric/integer" );
        result.setName ( "ErrorCount" );
        result.setValue ( Integer.toString ( Errors ) );
      }
      
      results = build.selectResult ( "WarningCount" );
      if ( results.toArray().length > 0 ) {
        result = results.toArray()[0];
      } else {
        result = resultFinder.newInstance();
        result.setTestId ( build.getTestId() );
        result.setType ( "numeric/integer" );
        result.setName ( "WarningCount" );
        result.setValue ( Integer.toString ( Warnings ) );
      }
      
      session.commit();
      session.flush();
    } catch ( Exception e ) {
      // rethrow
      throw e;
    } finally {
      connection.close();
    }

  }      
}
