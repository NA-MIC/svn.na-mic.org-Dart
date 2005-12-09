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

public class SummarizeDynamicAnalysis implements Task {
  static Logger logger = Logger.getLogger ( SummarizeDynamicAnalysis.class );
  JaxorContextImpl session;

  protected void rollup ( TestEntity test ) {
    Map accumulatedResults = new HashMap();
    
    // Walk the all the sub-tests
    TestIterator children = test.selectChildren().iterator();
    while ( children.hasNext() ) {
      TestEntity child = children.next();

      // rollup and child results
      rollup ( child );

      // rollup the results at this level
      ResultList results = child.getResultList();
      ResultIterator resultIt = results.iterator();
      while (resultIt.hasNext()) {
        ResultEntity result = resultIt.next();

        String resultType = result.getType();
        String resultName = result.getName();

        if (resultType.startsWith("numeric/")) {
          if (resultType.equals("numeric/integer")) {
            logger.debug("Summarizing " + result.getName() );
            // get the value of the child's result
            Integer value = (Integer) child.getResultValueAsObject(resultName,
                                                              new Integer(0));

            // get the current accumulated value at this level
            Integer current = new Integer(0);
            if (accumulatedResults.containsKey( resultName )) {
              current = (Integer) accumulatedResults.get( resultName );
            }

            // add and store the values
            Integer sum = new Integer( current.intValue() + value.intValue() );
            accumulatedResults.put( resultName, sum);

            logger.debug( "Value = " + value + ", Current = " + current + ", Sum = " + sum);
              
          } else if (resultType.equals("numeric/float")) {
            // get the value of the child's result
            Float value = (Float) child.getResultValueAsObject(resultName,
                                                              new Float(0));

            // get the current accumulated value at this level
            Float current = new Float(0);
            if (accumulatedResults.containsKey( resultName )) {
              current = (Float) accumulatedResults.get( resultName );
            }

            // add and store the values
            Float sum = new Float( current.intValue() + value.intValue() );
            accumulatedResults.put( resultName, sum);

          }
        }
        
      }
    }

    // Store the accumulated results in meta tests only
    try {
      if ( test.getStatus().equals( "m" ) ) {
        
        Iterator mapIt = accumulatedResults.entrySet().iterator();
        while (mapIt.hasNext()) {
          Map.Entry entry = (Map.Entry) mapIt.next();
          
          String key = (String)entry.getKey();
          Object value = entry.getValue();
          
          if (value instanceof Integer) {
            logger.debug("Setting " + key + " to " + (Integer) value);
            test.setResult( key, "numeric/integer",
                            ( (Integer) value).toString() );
          } else if (value instanceof Float) {
            test.setResult( key, "numeric/float",
                            ( (Float) value).toString() );
          }
        }
        test.getJaxorContext().commit();
      }
    } catch ( Exception e ) {
      logger.error( "Caught exception updating results.", e);
    }
  }

  public void execute ( Project project, Properties properties ) throws Exception {
    // Summarize the tests for a given submission
    String SubmissionId = properties.getProperty ( "SubmissionId" );
    Connection connection = project.getConnection();
    logger.debug ( "Starting DynamicAnalysis Summary for SubmissionId: " + SubmissionId ); 
    try {
      session = new JaxorContextImpl ( connection );
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
      TestFinderBase testFinder = new TestFinderBase ( session );
      SubmissionEntity submission = submissionFinder.selectBySubmissionId ( new Long ( SubmissionId ) );
      
      TestEntity dynamic = submission.selectTest ( ".DynamicAnalysis" );
      if ( dynamic == null ) {
        logger.debug ( project.getTitle() + ": No DynamicAnalysis info found" );
        return;
      }
    
      rollup ( dynamic );
      
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
