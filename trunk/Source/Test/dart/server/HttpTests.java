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
import com.meterware.httpunit.*;
import java.util.regex.*;

public class HttpTests extends TestCase {

  public HttpTests ( String s ) {
    super ( s );
  }
  Project project;
  WebConversation webConv;

  public void setUp() {
    project = DartServerTest.getProject();
    webConv = new WebConversation();
  }

  /** Verify that the webpage exists
   *
   * Does not verify the contents of the page in any way.
   */
  boolean hasContent ( String u ) throws Exception {
    URL url = new URL ( u );
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.connect();
    return ( conn.getResponseCode() == HttpURLConnection.HTTP_OK );
  }

  /** Verify that the webpage contains all the regexps provided.
   *
   * The regexps are matched using "find", not "matches", so that only
   * a substring within the webpage need match the regexp.
   */
  void assertPageContains( String url, String[] regexps ) throws Exception {
    WebResponse resp = webConv.getResponse( url );
    String webtext = resp.getText();
    // java.lang.System.out.println( "Text: "+webtext );
    for( int i = 0; i < regexps.length; ++i ) {
      Pattern p = Pattern.compile( regexps[i] );
      Matcher m = p.matcher( webtext );
      boolean r = m.find();
      // java.lang.System.out.println( "Find: "+regexps[i]+": "+r );
      assertTrue( r );
    }
  }
      


  public void testPingServer () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard" ) );
  }

  public void testSubmissionOverview () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/Submission?submissionid=1" ) );
  }

  public void testTestDetailPage() throws Exception {
    String[] contentRegExps =
      {
        "Insight/Code/Common",           // Test output is displayed
        "25.107994",     // Execution time is displayed
        "Completed",  // Completion status is displayed
        "Site Name:.*caleb\\.crd",       // Site name is displayed
        "Build Name:.*SunOS-c\\+\\+"     // Build name is displayed
      };
    assertPageContains( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/Test?testname=.Test.Testing.Code.Common.PrintSelf-Common&submissionid=1",
                         contentRegExps );
  }

  public void testChart () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Chart?type=time&history=30&title=zion.kitware-Linux-g%2B%2B-3.3-Nightly&xlabel=Date&ylabel=Time&legend=test&width=400&height=300&submissionid=1&testname=.Test.Testing.Code.Common.PrintSelf-Common" ) );
  }

  public void testNonexistentSubmissionOverview () throws Exception {
    assertFalse ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/Submission?submissionid=165443" ) );
  }

  public void testZipIndex () throws Exception {
    assertTrue ( hasContent ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Zip/0a/5c/98/0a5c98d515ea86d3598e00ad05428b0a.zip/index.html" ) );
  }

  public void testZipContent () throws Exception {
    String [] matches = {
      "Coverage Report - All Packages"
    };
    assertPageContains ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Zip/0a/5c/98/0a5c98d515ea86d3598e00ad05428b0a.zip/frame-summary.html", matches );
  }
    
  public void testTestPage () throws Exception {
    String [] matches = {
      "practical",
      "testDirectories"
    };
    assertPageContains ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/TestCatalog?trackid=2&submissionid=2&roottest=.Test.dart.server.ProjectLiveTestSuite", matches );
  }

  public void testTestPageAllSubtests () throws Exception {
    String [] matches = {
      "caleb.crd",
      "AutomaticMeshTest"
    };
    assertPageContains ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/TestCatalog?trackid=1&submissionid=1&roottest=&order=ascending&showall=1", matches );
  }
    
  public void testTestDetail () throws Exception {
    String [] matches = {
      "caleb.crd",
      "Mesh",
      "Observers:",
      "0.817079"
    };
    assertPageContains ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.projectName + "/Dashboard/Test?testname=.Test.Examples.DataRepresentation.Mesh.AutomaticMeshTest&submissionid=1", matches );
  }
    

  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new HttpTests ( "testPingServer" ) );
    tests.addTest ( new HttpTests ( "testSubmissionOverview" ) );
    tests.addTest ( new HttpTests ( "testTestDetailPage" ) );
    tests.addTest ( new HttpTests ( "testChart" ) );
    tests.addTest ( new HttpTests ( "testNonexistentSubmissionOverview" ) );
    tests.addTest ( new HttpTests ( "testZipIndex" ) );
    tests.addTest ( new HttpTests ( "testZipContent" ) );
    tests.addTest ( new HttpTests ( "testTestPage" ) );
    tests.addTest ( new HttpTests ( "testTestPageAllSubtests" ) );
    tests.addTest ( new HttpTests ( "testTestDetail" ) );
    return tests;
  }
}
