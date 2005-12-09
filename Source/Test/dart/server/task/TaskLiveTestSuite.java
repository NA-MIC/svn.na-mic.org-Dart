package dart.server.task;

import junit.framework.*;
import junit.extensions.*;
import java.io.*;
import java.util.Properties;
import java.net.URL;
import dart.*;
import dart.server.*;
import dart.server.task.*;
import org.apache.log4j.Logger;
import org.apache.log4j.*;

public class TaskLiveTestSuite extends TestCase {
  static Logger logger = Logger.getLogger ( TaskLiveTestSuite.class );   

  public TaskLiveTestSuite ( String s ) {
    super ( s );
  }
  Project project;

  public void setUp() {
    project = DartServerTest.getProject();
  }

  public void testSaveStatistics() throws Exception {
    // Get the live project
    Task task = (Task)Class.forName ( "dart.server.task.SaveStatistics" ).newInstance();
    task.execute ( project, null );
    assertTrue ( "Statistics saved", new File ( DartServerTest.getProjectDirectory(), "Statistics.txt" ).exists() );
  }
  
  public void testProcessXMLShort() throws Exception {
    // Find the file
    URL url = DartServer.class.getClassLoader().getResource ( "dart/Resources/Test/Test.xml.gz" );
    logger.info ( "URL: " + url );
    // Find the task
    Task task = (Task)Class.forName ( "dart.server.task.XMLResultProcessor" ).newInstance();
    Properties prop = new Properties();
    prop.setProperty ( "URL", url.toString() );
    task.execute ( project, prop );
  }

  public void testProcessXMLLong() throws Exception {
    // Find the file
    URL url = DartServer.class.getClassLoader().getResource ( "dart/Resources/Test/TestLong.xml.gz" );
    // Find the task
    Task task = (Task)Class.forName ( "dart.server.task.XMLResultProcessor" ).newInstance();
    Properties prop = new Properties();
    prop.setProperty ( "URL", url.toString() );
    task.execute ( project, prop );
  }

  public void testProcessXMLLongCorrect() throws Exception {
    // Find the file
    URL url = DartServer.class.getClassLoader().getResource ( "dart/Resources/Test/TestLongCorrect.xml.gz" );
    // Find the task
    Task task = (Task)Class.forName ( "dart.server.task.XMLResultProcessor" ).newInstance();
    Properties prop = new Properties();
    prop.setProperty ( "URL", url.toString() );
    task.execute ( project, prop );
  }

  public void testQueue() throws Exception {
    Task task = (Task)Class.forName ( "dart.server.task.QueueManager" ).newInstance();
    task.execute ( project, new Properties() );
  }
    

  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new TaskLiveTestSuite ( "testSaveStatistics" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testProcessXMLShort" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testProcessXMLLong" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testProcessXMLLongCorrect" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testQueue" ) );
    return tests;
  }
}
