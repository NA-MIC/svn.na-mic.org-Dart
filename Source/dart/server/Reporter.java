package dart.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.*;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import dart.DartServer;
import dart.server.task.ScheduledTask;
import dart.server.task.Task;
import dart.server.wrap.*;


public class Reporter {
  static Logger logger = Logger.getLogger ( Reporter.class );   

  HashMap reports = new HashMap();
  Project project = null;

  public Reporter() {
    logger.debug ( "Creating Reporter" );
  }

  public void shutdown () throws Exception {
    logger.debug ( project.getTitle() + ": shutdown Reporter" );
  }
  public void start ( Project p ) {
    project = p;
    logger.debug ( project.getTitle() + ":Starting Reporter" ); 
  }

  public void addReport ( Report r ) {
    reports.put ( r.getName(), r );
  }
}


