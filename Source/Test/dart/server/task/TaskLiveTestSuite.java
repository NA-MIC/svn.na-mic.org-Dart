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
  
  public void testGarbageCollection() throws Exception {
    // Get the live project
    Task task = (Task)Class.forName ( "dart.server.task.GarbageCollectionTask" ).newInstance();
    task.execute ( project, null );
  }
  
  public void testDeleteDataTask() throws Exception {
    // Get the live project
    Task task = (Task)Class.forName ( "dart.server.task.DeleteDataTask" ).newInstance();
    Properties props = new Properties();
    props.put ( "ResultValue", "1" );
    task.execute ( project, props );
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

  public void testProcessXMLWithZip() throws Exception {
    // Find the file
    URL url = DartServer.class.getClassLoader().getResource ( "dart/Resources/Test/TestWithZip.xml.gz" );
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
    
  public void testArchiveTask() throws Exception {
    // Get the live project
    Task task = (Task)Class.forName ( "dart.server.task.ArchiveTask" ).newInstance();

    File f = new File ( DartServerTest.getProjectDirectory(), "Archive" );
    
    Properties properties = new Properties();
    properties.setProperty ( "ArchiverList", "Test" );
    properties.setProperty ( "ArchiverList.Test.ArchiveDirectory", f.getPath() + File.separator + "Foo" );
    task.execute ( project, properties );
    assertTrue ( "Archive created", new File ( f, "Working" ).exists() );
  }

  void testSubmissionTask ( String className ) throws Exception {
    // Get the live project
    Task task = (Task)Class.forName ( className ).newInstance();
    Properties properties = new Properties();
    properties.setProperty ( "SubmissionId", "1" );
    properties.setProperty ( "Domains", "Build,Test,Coverage,DynamicAnalysis" );
    properties.setProperty ( "TrackName", "Nightly" );
    try {
      logger.error ( "Project: " + project + " Task: " + task );
      task.execute ( project, properties );
    } catch ( Exception e ) {
      fail( e.toString() );
    }
  }
  
  public void testSummarizeBuildTask() throws Exception {
    testSubmissionTask ( "dart.server.task.SummarizeBuildTask" );
  }

  public void testSummarizeCoverage() throws Exception {
    testSubmissionTask ( "dart.server.task.SummarizeCoverage" );
  }

  public void testSummarizeDynamicAnalysis() throws Exception {
    testSubmissionTask ( "dart.server.task.SummarizeDynamicAnalysis" );
  }

  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new TaskLiveTestSuite ( "testSaveStatistics" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testGarbageCollection" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testDeleteDataTask" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testProcessXMLLongCorrect" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testProcessXMLWithZip" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testQueue" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testArchiveTask" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testSummarizeBuildTask" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testSummarizeCoverage" ) );
    tests.addTest ( new TaskLiveTestSuite ( "testSummarizeDynamicAnalysis" ) );

    return tests;
  }
}
