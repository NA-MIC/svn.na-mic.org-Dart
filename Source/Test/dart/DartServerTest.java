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

public class DartServerTest extends TestCase {

  public static int PortNumber = 40001;
  public DartServerTest ( String s ) {
    super ( s );
  }

  static Project project = null;
  static Server server = null;
  static Logger logger = Logger.getLogger ( DartServerTest.class );   

  static String path = System.getProperties().getProperty( "java.io.tmpdir" ) + File.separator + System.getProperties().getProperty ( "user.name" );
  public static String projectName = "TestProject";
  public static String serverName = "DartServer";
  static public String getPath() { return path; }
  static public String getProjectName() { return projectName; }
  static public Project getProject() { return project; }
  static public File getProjectDirectory() { return new File ( path + File.separator + projectName ); }


  public void testCreate() throws Exception {
    Server.createServer ( path + File.separator + serverName, "derby" );
    assertTrue ( new File ( path + File.separator + serverName + File.separator + "Server.xml" ).exists() );
    Project.createProject ( path + File.separator + projectName, "derby", null );
    assertTrue ( new File ( path + File.separator + projectName + File.separator + "Project.xml" ).exists() );
  }

  public void testLoad() {
    server = Server.loadServer ( path + File.separator + serverName );
    // Set high port numbers
    server.setHttpPort ( String.valueOf ( PortNumber ) );
    assertNotNull ( server );
  }

  public void testStart() throws Exception {
    // Start the server db
    server.getDatabase().start ( server );
    server.initializeDatabase();
    server.setInitializeProjectDB ( true );
    server.setRefreshProjectResources ( true );
    server.clearProjects ();
    server.addProject ( path + File.separator + projectName );
    server.start ( false );
    // Load the project
    project = server.getProject ( projectName );
    assertNotNull ( project );
  }

  public void testCreateSchema() {
    assertTrue ( new File ( path + File.separator + projectName + File.separator + "Schema.sql" ).exists() );
  }

  public void testShutdown () throws Exception {
    server.doShutdown();
  }

  public static Test suite() {
    TestSuite tests = new TestSuite();
    tests.addTest ( new DartServerTest ( "testCreate" ) );
    tests.addTest ( new DartServerTest ( "testLoad" ) );
    tests.addTest ( new DartServerTest ( "testStart" ) );
    tests.addTest ( new DartServerTest ( "testCreateSchema" ) );
    tests.addTest ( ProjectLiveTestSuite.suite() );
    tests.addTest ( new DartServerTest ( "testShutdown" ) );
    
    TestSetup setup = new TestSetup(tests) {
        protected void setUp( ) throws Exception {
          URL logConfigurationFile = DartServer.class.getClassLoader().getResource ( "dart/Resources/Server/log4j.properties" );
          // BasicConfigurator.configure();
          PropertyConfigurator.configure ( logConfigurationFile );
          
          // server = new Server ( );
        }
        protected void tearDown( ) throws Exception {
          // do your one-time tear down here!
        }
      };
    return setup;
  }

  public class DartServerTestSetup extends TestSetup {
    public DartServerTestSetup ( TestSuite s ) {
      super ( s );
    }
  }


}
