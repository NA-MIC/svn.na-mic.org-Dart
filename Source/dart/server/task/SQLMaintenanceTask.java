package dart.server.task;

import java.io.File;
import java.io.*;
import java.sql.*;
import java.net.URL;
import java.util.Properties;

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

/**
 * SQLMaintenanceTask executes commands in a file for maintance of a
 * Dart database.  The format of the file is as follows:
 *
 * <code>select * from test;</code>
 *
 * Each command is executed independantly and is terminated by a semi
 * colon.  The SQL comment character is the double minus, i.e., "--".
 * Any text following the comment character is ignored to the end of
 * line.  Comments may appear in the body of other statements.
 *
 * Several properties control the behavior of the task.  They are:
 * <p>
 * <code>Filename</code> indicates the path to the file containing the
 * SQL commands.  The path name is prepended with the project
 * directory.
 * <p>
 * <code>ContinueOnError</code> is a boolean instructing Dart to
 * continue of halt when an SQL error is encountered.  Default is
 * <code>true</code>. All errors are
 * logged.
 */
public class SQLMaintenanceTask implements Task {
  static Logger logger = Logger.getLogger ( SQLMaintenanceTask.class );   

  public void execute ( Project project, Properties properties ) throws Exception {

    // Execute the SQL commands for the project.
    if ( properties.getProperty ( "Filename", "" ).equals ( "" ) ) {
      logger.warn ( project.getTitle() + ": no Filename property given, no SQL executed" );
      return;
    }
    File SQLFile = new File ( project.getBaseDirectory(), properties.getProperty ( "Filename", "NonexistantSQLFile.sql" ) );
    if ( !SQLFile.exists() ) {
      logger.warn ( project.getTitle() + ": File " + SQLFile + " does not exist" );
      return;
    }
    boolean ContinueOnError = Boolean.valueOf ( properties.getProperty ( "ContinueOnError", "true" ) ).booleanValue();

    // Execute the file
    project.executeSQL ( SQLFile, ContinueOnError );
  }
}
