package dart.server;

import java.util.*;
import org.apache.log4j.*;

public class TestProxy {
  static Logger logger = Logger.getLogger ( TestProxy.class );   
  
  String status = "passed";
  String name = null;
  String qualifiedname = null;
  String path = null;
  HashMap<String,Object> map = new HashMap<String,Object>();
  HashMap<String,Integer> repeated = new HashMap<String,Integer>();
  boolean v1compatability = true;
  ArrayList<TestProxy> children = null;
  boolean isrepeated = false;
  String testClass = ".Test";
  boolean allowDuplicates = false;
  // boolean ignoreDuplicate = true;

  
  public boolean allowDuplicates () { return allowDuplicates; }
  public void setAllowDuplicates ( boolean i ) { allowDuplicates = i; }
  public void setAllowDuplicates ( String i ) {
    allowDuplicates = Boolean.valueOf(i).booleanValue();
    logger.debug(name + ", setting ignore duplicate: " + i);
  }

  public void replaceClass ( String old, String c ) { 
    qualifiedname = qualifiedname.replaceFirst ( old, c );
    logger.debug ( "replaceClass: " + qualifiedname );
  }
  public ArrayList getChildren () { return children; }
  public void addChild ( TestProxy p ) {
    logger.debug ( "Adding child: " + p.getQualifiedName() );
    if ( children == null ) {
      children = new ArrayList<TestProxy>();
    }
    children.add ( p );
  }

  public TestProxy () {
    logger.debug ( "Creating TestProxy" );
  }
  
  public void setRepeated ( String r ) { isrepeated = Boolean.valueOf ( r ).booleanValue(); }
  public void setRepeated ( boolean r ) { isrepeated = r; }
  public boolean isRepeated () { return isrepeated; }

  public void setV1 ( boolean v ) { v1compatability = v; }
  public void setV1 ( Boolean v ) { setV1 ( v.booleanValue() ); }

  public void log ( Object o ) { logger.debug ( "Log in TestProxy" ); }
  public void setName ( String n ) {
    if ( v1compatability ) {
      name = testClass + n; 
    } else {
      name = n;
    }
    logger.debug ( "Set name to " + name ); 
  }

  public void setJUnitName ( String classname, String name ) {
    logger.debug ( "setJUnitName: " + classname + "." + name );
    qualifiedname = testClass + "." + classname + "." + name;
  }

  public void setQualifiedNameNoReplace ( String n ) {
    logger.debug ( "setQualifiedNameNoReplace: " + n );
    qualifiedname = n;
  }
  public void setQualifiedName ( String n ) {
    qualifiedname = testClass + n.replaceAll ( "\\./", "/").replaceAll ( "\\.", "_").replaceAll ( "/", "\\." );
    logger.debug ( "setQualifiedName: " + qualifiedname );
  }
  public String getQualifiedName ( ) { return qualifiedname; }
  public String getName ( ) { return name; }
  public void setPath ( String p ) { path = p; }
  public String getPath ( ) { return path; }
  public void setStatus ( String p ) {
    if (p.equals("true")) {
      p = "passed";
    } else if (p.equals("false")) {
      p = "failed";
    }
    status = p;
  }
  public String getStatus () {
    if ( status.toLowerCase().equals ( "passed" ) ) {
      return "p";
    } else if ( status.toLowerCase().equals ( "failed" ) ) {
      return "f";
    } 
    return "n";
  }

  public void incrementNamedInteger ( String Name ) {
    incrementNamedInteger ( Name, 1 ); 
  }
  public void incrementNamedInteger ( String Name, int i ) {
    String Value = "1";
    if ( map.containsKey ( Name ) ) {
      String[] r = (String[]) map.get ( Name );
      int v = Integer.parseInt ( r[1] ) + 1;
      Value = Integer.toString ( v );
    }    
    setNamedResult ( Name, "numeric/integer", Value );
  }      

  public void setCruiseControlStatus ( String BaseTestName, String priority, String Value ) {
    logger.debug ( "Calling setCruiseControlStatus " + BaseTestName + " " + priority + " " + Value );
    if ( priority.equals ( "debug" ) ) { return; }
    setRepeated ( true );
    if ( priority.equals ( "warn" ) ) {
      setQualifiedNameNoReplace ( BaseTestName + ".Warning" );
    }
    if ( priority.equals ( "error" ) ) {
      setQualifiedNameNoReplace ( BaseTestName + ".Error" );
    }
    setNamedResult ( "Text", "text/string", Value );
  }

  public void appendCruiseControlMessage ( String Name, String priority, String Value ) {
    logger.debug ( "appendCruiseControlMessage " + Name + ": " + Value );

    if ( priority.equals ( "info" ) ) {
    } else if ( priority.equals ( "error" ) ) {
      Value = "<font color=\"#ff0000\"><b>" + Value + "</b></font>";
      incrementNamedInteger ( "ErrorCount" );
    } else if ( priority.equals ( "warn" ) ) {
      incrementNamedInteger ( "WarningCount" );
      Value = "<font color=\"#ffff00\"><b>" + Value + "</b></font>";
    }
    Value = Value + "<br>\n";
    if ( map.containsKey ( Name ) ) {
      String[] r = (String[]) map.get ( Name );
      Value = r[1] + Value;
      logger.debug ( "appendCruiseControlMessage: appending to existing!" );
    }

    setNamedResult ( Name, "text/html", Value );
  }    


  public void appendNamedResult ( String Name, String Type, String Value ) {
    if ( !map.containsKey ( Name + Type ) ) {
      // Append to it
      setNamedResult ( Name, Type, Value );
    } else {
      String[] r = (String[]) map.get ( Name + Type );
      r[1] = r[1] + "\n" + Value;
      map.put ( Name + Type, r );
    }
  }    

  public void appendCoverageLine ( String count, String line ) {
    // See if there is a result already
    String l = "       ";
    if ( !count.equals ( "-1" ) ) {
      l = "";
      for ( int i = 0; i < 6 - count.length(); i++ ) {
        l = l + " ";
      }
      l = l + count + " ";
    }
    l = l + line;

    // Do we have something to append to
    StringBuffer output = new StringBuffer ();
    if ( !map.containsKey ( "Output" ) ) {
      // Append to it
      output.append ( "<pre>" );
    } else {
      String[] r = (String[]) map.get ( "Output" );
      // Strip off the trailing </pre>
      int idx = r[1].lastIndexOf ( "</pre>" );
      output.append ( r[1].substring ( 0, idx ) );
    }
    
    output.append ( l + "\n</pre>" );
    setNamedResult ( "Output", "text/html", output.toString() );
    logger.debug ( "Formatted\n" + line + "\n\t to \n" + l );
      
  }
      

  public void setNamedResult ( String Name, String Type, String Value ) {
    // logger.debug ( "setNamedResult: " + Name + " " + Type + " " + Value );
    map.put ( Name, new String[] { Type, Value } );
  }

  public void setRepeatedNamedResult ( String Name, String Type, String Value ) {
    logger.debug ( "setRepeatedNamedResult: " + Name + " " + Type + " " + Value );
    // Find the result in the repeated
    if ( !repeated.containsKey ( Name ) ) {
      repeated.put ( Name, new Integer ( 0 ) );
    }
    Integer i = (Integer)repeated.get ( Name );
    map.put ( Name + i, new String[] { Type, Value } );
    repeated.put ( Name, new Integer ( i.intValue() + 1 ) );
  }

  public HashMap getResultMap () { return map; }

  public void setResult ( String Value ) {
    logger.debug ( "setResult: " + Value );
    setNamedResult ( "Output", "text/text", Value );
  }

}


