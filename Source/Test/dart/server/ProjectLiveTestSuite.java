package dart.server;

import junit.framework.*;
import junit.extensions.*;
import java.io.*;
import java.util.*;
import java.util.Properties;
import java.net.URL;
import dart.*;
import dart.server.*;
import dart.server.task.*;

public class ProjectLiveTestSuite extends TestCase {

  public ProjectLiveTestSuite ( String s ) {
    super ( s );
  }
  Project project;

  public void setUp() {
    project = DartServerTest.getProject();
  }

  public void testRefresh () {
    project.refreshResources();
    assertTrue ( "Templates exist", new File ( project.getTemplates(), "Dashboard.ftl" ).exists() );
    assertTrue ( "Resources exist", new File ( project.getHtmlDirectory(), "Resources" + File.separator + "Style.css" ).exists() );
  }

  public void testDirectories () {
    assertNotNull ( project.getUniqueFileInTemporaryDirectory ( ".txt" ) );
  }

  public void testMD5 () {
    byte[] b = new byte[2];
    b[0] = 0xf;
    b[1] = 0x1;
    assertNotNull ( project.generateProjectRelativeFileForBinary ( b, ".txt" ) );
  }
  
  public void testStatistics () {
    project.incrementStatistic ( "Foo" );
    assertTrue ( "1".equals ( project.getStats().get ( "Foo" ) ) );
  }

  public void testParseBuildStamp () {
    Date date = project.parseBuildStamp ( "20050107-0500-Nightly" );
    Calendar c = Calendar.getInstance();
    c.setTime ( date );

    Date dateUTC = project.parseBuildStamp ( "2005-01-07T05:00:00.000-0000" );
    Calendar cUTC = Calendar.getInstance();
    cUTC.setTime ( date );

    assertTrue ( c.get ( Calendar.HOUR ) == cUTC.get ( Calendar.HOUR ) );
    assertTrue ( c.get ( Calendar.MINUTE ) == cUTC.get ( Calendar.MINUTE ) );
    assertTrue ( c.get ( Calendar.SECOND ) == cUTC.get ( Calendar.SECOND ) );
    
  }


  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new ProjectLiveTestSuite ( "testRefresh" ) );
    tests.addTest ( new ProjectLiveTestSuite ( "testDirectories" ) );
    tests.addTest ( new ProjectLiveTestSuite ( "testMD5" ) );
    tests.addTest ( new ProjectLiveTestSuite ( "testStatistics" ) );
    tests.addTest ( new ProjectLiveTestSuite ( "testParseBuildStamp" ) );
    tests.addTest ( TaskLiveTestSuite.suite() );
    return tests;
  }
}
