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

public class ArchiveTaskTests extends TestCase {
  static Logger logger = Logger.getLogger ( ArchiveTaskTests.class );   

  public ArchiveTaskTests ( String s ) {
    super ( s );
  }
  Project project;

  public void setUp() {
    project = DartServerTest.getProject();
  }
  public void testArchiveTask() throws Exception {
    // Get the live project
    Task task = (Task)Class.forName ( "dart.server.task.ArchiveTask" ).newInstance();

    File f = new File ( DartServerTest.getProjectDirectory(), "Archive" );
    
    Properties properties = new Properties();
    properties.setProperty ( "ArchiverList", "Test" );
    properties.setProperty ( "Archiver.Test.ArchiveDirectory", f.getPath() + File.separator + "Foo" );
    properties.setProperty ( "Archiver.Test.ArchiveLevel", "4" );
    properties.setProperty ( "Archiver.Test.AgeInDays", "0.00001" );
    task.execute ( project, properties );
    assertTrue ( "Archive created", new File ( f.getPath() + File.separator + "Foo" + File.separator +  "Working" ).exists() );
  }

  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new ArchiveTaskTests ( "testArchiveTask" ) );
    return tests;
  }
}
