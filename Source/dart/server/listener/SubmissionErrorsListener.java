package dart.server.listener;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;
import java.text.*;
import java.util.Properties;

// import marquee.xmlrpc.XmlRpcServer;

import dart.server.task.ScheduledTask;
import dart.server.event.*;
import dart.server.task.Task;
import dart.server.task.*;
import dart.server.*;
import org.apache.log4j.Logger;

public class SubmissionErrorsListener extends Listener {
  static Logger logger = Logger.getLogger ( SubmissionErrorsListener.class );

  public void trigger ( Project project, SubmissionEvent event ) throws Exception {
    logger.info ( "triggered on SubmissionEvent" );
    logger.info ( "Got submission event for SubmissionId " + event.getSubmissionId() );
  }

}

