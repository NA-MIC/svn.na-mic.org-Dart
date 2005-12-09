package dart.server;

import java.util.ArrayList;

import org.apache.log4j.Logger;


public class Report {
  static Logger logger = Logger.getLogger ( Report.class );   

  String name = null, type = null, nameMatch = null, buildNameMatch = null,
    classMatch = null, testMatch = null, template = null;
  int priority = 10;
  ArrayList nextReports = new ArrayList();

  public Report() {
    logger.info ( "Creating Report" );
  }

  public void setName ( String n ) { name = n; }
  public String getName () { return name; }
  public void setType ( String t ) { type = t; }
  public void setNameMatch ( String m ) { nameMatch = m; }
  public void setBuildNameMatch ( String m ) { buildNameMatch = m; }
  public void setClassMatch ( String m ) { classMatch = m; }
  public void setTestMatch ( String m ) { testMatch = m; }

  public void setTemplate ( String t ) { template = t; }
  public void setPriority ( String p ) { priority = Integer.parseInt ( p ); }
  public void addNext ( String n ) { nextReports.add ( n ); }

}


