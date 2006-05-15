package dart.server.servlet;


import dart.server.*;
import freemarker.core.*;
import freemarker.template.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
   Simple class to import binary data into a Freemarker Template
*/

public class FetchData implements TemplateMethodModel {
  Project project;
  public FetchData ( Project p ) {
    project = p;
  }
  public Object exec ( List args ) throws TemplateModelException {
    if ( args.size() != 1 ) {
      throw new TemplateModelException ( "usage: <filename>" );
    }
    // Read the gzipped file
    String filename = (String)args.get ( 0 );
    File file = new File ( project.getDataDirectory() + File.separator + filename );
    if ( !file.exists() || !file.canRead() ) {
      throw new TemplateModelException ( "File " + file.toString() + " does not exist or is not readable" );
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      InputStream in = new BufferedInputStream ( new FileInputStream ( file ) );
      if ( filename.endsWith ( ".gz" ) ) {
        in = new GZIPInputStream ( in );
      }
      byte[] buffer = new byte[2048];
      int count;
      while ( (count = in.read ( buffer )) != -1 ) {
        out.write ( buffer, 0, count );
      }
      in.close();
      return out.toString();
    } catch ( Exception e ) {
      throw new TemplateModelException ( "Failed to read file", e );
    }
    // throw new TemplateModelException ( "Should never get here!" )
  }
}
