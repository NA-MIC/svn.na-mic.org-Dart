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
import java.net.*;

public class HttpTests extends TestCase {

  public HttpTests ( String s ) {
    super ( s );
  }
  Project project;

  public void setUp() {
    project = DartServerTest.getProject();
  }

  boolean hasContent ( String u ) throws Exception {
    URL url = new URL ( u );
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.connect();
    return ( conn.getResponseCode() == HttpURLConnection.HTTP_OK );
  }

  public void testPingServer () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard" ) );
  }

  public void testSubmissionOverview () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/Submission?submissionid=1" ) );
  }

  public void testTestDetailPage () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/Test?testname=.Test.Testing.Code.Common.PrintSelf-Common&submissionid=1" ) );
  }

  public void testChart () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Chart?type=time&history=30&title=zion.kitware-Linux-g%2B%2B-3.3-Nightly&xlabel=Date&ylabel=Time&legend=test&width=400&height=300&submissionid=1&testname=.Test.Testing.Code.Common.PrintSelf-Common" ) );
  }

  public void testNonexistentSubmissionOverview () throws Exception {
    assertFalse ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/Submission?submissionid=165443" ) );
  }

  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new HttpTests ( "testPingServer" ) );
    tests.addTest ( new HttpTests ( "testSubmissionOverview" ) );
    tests.addTest ( new HttpTests ( "testTestDetailPage" ) );
    tests.addTest ( new HttpTests ( "testChart" ) );
    tests.addTest ( new HttpTests ( "testNonexistentSubmissionOverview" ) );
    tests.addTest ( TaskLiveTestSuite.suite() );
    return tests;
  }
}
