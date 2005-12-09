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

public class ReportTask implements Task {
  static Logger logger = Logger.getLogger ( ReportTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {

    // See what sort of report to generate
    String type = properties.getProperty ( "Type" );
    String templateName = properties.getProperty ( "Template" );
    
    // Get a FreeMarker configuration engine
    Configuration cfg = new Configuration();
    cfg.setClassForTemplateLoading ( ReportTask.class, "/" );
    // The following template is not going to be found.  It is now
    // held under Project's HTML
    Template template = cfg.getTemplate ( "Templates" + templateName );
    File output = null;
    Map root = new HashMap();
    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );

    if ( type.equals ( "Submission" ) ) {
      // Find and fill in the build information
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );
      try {
        SubmissionEntity submission = submissionFinder.selectBySubmissionId ( new Long ( properties.getProperty ( "SubmissionId" ) ) );
        root.put ( "Submission", submission );

        // Find the correct place to put it
        ClientEntity client = submission.getClientEntity();
        logger.debug ( "Found client: " + client );
        root.put ( "Client", client );
        output = new File ( project.getBaseDirectory() 
                            + File.separator + "HTML" 
                            + File.separator + client.getSite() 
                            + File.separator + client.getBuildName()
                            + File.separator + submission.getTimeStamp()
                            + "-" + submission.getType()
                            + File.separator + "Submission.html" );
      } catch ( Exception e ) {
        logger.error ( project.getTitle() + ": Error", e );
        throw e;
      }
    } else if ( type.equals ( "Dashboard" ) ) {
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( session );

      java.util.Date date = new SimpleDateFormat().parse ( properties.getProperty ( "Date" ) );
      logger.info ( project.getTitle() + ": Parsed date " + date );
      String ts = new SimpleDateFormat( "yyyyMMdd" ).format ( date );
      root.put ( "Date", date );
      root.put ( "Timestamp", ts );

      output = new File ( project.getBaseDirectory()
                          + File.separator + "HTML"
                          + File.separator + "Dashboard"
                          + File.separator + ts
                          + File.separator + "Dashboard.html" );

      // Select the relevant builds
      Calendar cal = Calendar.getInstance();
      cal.setTime ( date );
      cal.set ( Calendar.HOUR_OF_DAY, 0 );
      cal.set ( Calendar.SECOND, 0 );
      cal.set ( Calendar.MILLISECOND, 0 );
      java.util.Date start = cal.getTime();

      cal = Calendar.getInstance();
      cal.setTime ( start );
      cal.add ( Calendar.HOUR_OF_DAY, 24 );
      java.util.Date end = cal.getTime();
      QueryParams params = new QueryParams();
      java.sql.Date d1 = new java.sql.Date ( start.getTime() );
      java.sql.Date d2 = new java.sql.Date ( end.getTime() );
      params.add ( d1 );
      params.add ( d2 );
      logger.debug ( project.getTitle() + ": Select from " + d1 + " to " + d2 );
      SubmissionList list = submissionFinder.find ( "where timestamp >= ? and timestamp < ?", params );
      logger.debug ( project.getTitle() + ": Found " + list.toArray().length + " builds for the dashboard" );

      logger.debug ( project.getTitle() + ": Build Timestamp" + list.toArray()[0].getTimeStamp() );
      root.put ( "Submissions", list );
      
    } else {
      logger.warn ( project.getTitle() + ": Not ready to do other dashboards" );
      return;
    }
    
    try {
      // Run the template
      output.getParentFile().mkdirs();
      Writer out = new BufferedWriter ( new FileWriter ( output ) );
      template.process ( root, out );
      out.flush();
    } catch ( Exception e ) {
      logger.error ( project.getTitle() + ": Failed to process template", e );
    }
  }
}
