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

public class SummarizeCoverage implements Task {
  static Logger logger = Logger.getLogger ( SummarizeCoverage.class );   
  JaxorContextImpl session;

  protected void rollup ( TestEntity coverage ) {
    int testedLines = 0;
    int untestedLines = 0;
      
    // Walk the all the sub-tests
    TestIterator children = coverage.selectChildren().iterator();
    while ( children.hasNext() ) {
      TestEntity child = children.next();
      // Accumulate coverage info
      rollup ( child );
      Object LOCTested = child.getResultValueAsObject ( "LOCTested", null );
      Object LOCUnTested = child.getResultValueAsObject ( "LOCUnTested", null );
      if ( LOCTested != null && LOCUnTested != null ) {
        testedLines += ((Integer)LOCTested).intValue();
        untestedLines += ((Integer)LOCUnTested).intValue();
        logger.debug ( "Incremented by " 
                       + ((Integer)LOCTested).intValue()
                       + " / " 
                       + ((Integer)LOCUnTested).intValue()
                       + " from test: " + child.getName() );
      }
    }
    try {
      // Pop it in to Meta tests only
      if ( coverage.getStatus().equals ( "m" ) ) {
        coverage.setResult ( "LOCTested", "numeric/integer", Integer.toString ( testedLines ) );
        coverage.setResult ( "LOCUnTested", "numeric/integer", Integer.toString ( untestedLines ) );

        int totalLines = testedLines + untestedLines;

        if (totalLines > 0) {
          coverage.setResult ( "PercentCoverage", "numeric/float",
                 Float.toString( (testedLines
                             / (float) totalLines ) * 100.0f ) );

          // The coverage metric is a biased percentage measurement
          // that does not penalize small files
          coverage.setResult ( "CoverageMetric", "numeric/float",
                 Float.toString( ((testedLines + 10)
                             / (float)(totalLines + 10)) ) );
        } else {
          coverage.setResult( "PercentCoverage", "numeric/float", "0.0");
          coverage.setResult( "CoverageMetric", "numeric/float", "1.0");
        }
        logger.debug ( "Setting coverage for " + coverage.getName() + " to " + testedLines + " / " + untestedLines );

        coverage.getJaxorContext().commit();
      }
    } catch ( Exception e ) {
      logger.error ( "Caught exception updating LOCTested/LOCUnTested", e );
    }
  }

  public void execute ( Project project, Properties properties ) throws Exception {
    // Summarize the tests for a given submission
    String SubmissionId = properties.getProperty ( "SubmissionId" );
    Connection connection = project.getConnection();
    logger.debug ( "Starting Coverage Summary for SubmissionId: " + SubmissionId ); 
    try {
      session = new JaxorContextImpl ( connection );
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
      TestFinderBase testFinder = new TestFinderBase ( session );
      SubmissionEntity submission = submissionFinder.selectBySubmissionId ( new Long ( SubmissionId ) );
      
      TestEntity coverage = submission.selectTest ( ".Coverage" );
      if ( coverage == null ) {
        logger.debug ( project.getTitle() + ": No Coverage info found" );
        return;
      }
    
      rollup ( coverage );
      
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
