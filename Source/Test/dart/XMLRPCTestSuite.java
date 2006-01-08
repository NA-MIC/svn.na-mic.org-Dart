package dart;

import junit.framework.*;
import junit.extensions.*;
import dart.*;
import dart.server.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.security.*;
import java.io.*;

import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import java.util.Vector;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;
import org.apache.log4j.Logger;
import org.apache.log4j.*;
import org.quartz.Scheduler;
import org.quartz.impl.DirectSchedulerFactory;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;

import dart.server.Project;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class XMLRPCTestSuite extends TestCase {
  static Logger logger = Logger.getLogger ( XMLRPCTestSuite.class );   

  public XMLRPCTestSuite ( String s ) {
    super ( s );
  }

  public void testGetStatus () throws Exception {
    XmlRpcClient admin = new XmlRpcClient ( new URL ( "http://localhost:" + DartServerTest.PortNumber + "/DartServer/Command/" ) );
    String o = (String)admin.execute ( "Administration.getStatus", new Vector() );
    logger.info ( "Status: " + o );
  }

  public void testServerRefresh () throws Exception {
    XmlRpcClient admin = new XmlRpcClient ( new URL ( "http://localhost:" + DartServerTest.PortNumber + "/DartServer/Command/" ) );
    admin.execute ( "Administration.refresh", new Vector() );
  }

  public void testProjectRefresh () throws Exception {
    XmlRpcClient client = new XmlRpcClient ( new URL ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.project.getTitle() + "/Command/" ) );
    client.execute ( "Submit.refresh", new Vector() );
  }

  public void testSubmit () throws Exception {
    byte[] Data = new byte[32000];

    // Open the connection to the server
    XmlRpcClient client = new XmlRpcClient ( new URL ( "http://localhost:" + DartServerTest.PortNumber + "/" + DartServerTest.project.getTitle() + "/Command/" ) );

    // Read in the file
    URL url = DartServer.class.getClassLoader().getResource ( "dart/Resources/Test/TestLongCorrect.xml.gz" );
    InputStream in = url.openStream();
    ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
    OutputStream out = new BufferedOutputStream ( bytes );
    while ( true ) {
      int bytesRead = 0;
      bytesRead = in.read ( Data );
      if ( bytesRead == -1 ) {
      break;
      }
      out.write ( Data, 0, bytesRead );
    }
    in.close();
    Vector params = new Vector();
    params.addElement ( bytes.toByteArray() );
    client.execute ( "Submit.put", params );
  }


  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new XMLRPCTestSuite ( "testGetStatus" ) );
    tests.addTest ( new XMLRPCTestSuite ( "testServerRefresh" ) );
    tests.addTest ( new XMLRPCTestSuite ( "testProjectRefresh" ) );
    tests.addTest ( new XMLRPCTestSuite ( "testSubmit" ) );
    return tests;
  }

}
