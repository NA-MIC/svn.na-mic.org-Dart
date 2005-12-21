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
import org.apache.xmlrpc.XmlRpcServer;
import net.sourceforge.jaxor.JaxorContextImpl;
import net.sourceforge.jaxor.QueryParams;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.mortbay.http.DigestAuthenticator;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.handler.NotFoundHandler;
import org.mortbay.http.handler.ResourceHandler;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import dart.server.task.ScheduledTask;
import dart.server.event.*;
import dart.server.task.Task;
import dart.server.task.*;
import dart.server.wrap.TaskQueueEntity;
import dart.server.wrap.TaskQueueFinderBase;
import dart.server.servlet.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.log4j.Logger;

public class SubmissionErrorsListener extends Listener {
  static Logger logger = Logger.getLogger ( SubmissionErrorsListener.class );

  public void trigger ( Event e ) throws Exception {
    SubmissionEvent event;
    if ( e instanceof SubmissionEvent ) {
      event = (SubmissionEvent) e;
    } else {
      return;
    }
    logger.info ( "Got submission event for SubmissionId " + event.getSubmissionId() );
  }
}

