package dart.server.servlet;

import java.io.*;
import java.util.zip.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.http.*;
import javax.servlet.*;

import java.sql.Connection;
import net.sourceforge.jaxor.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.*;
import freemarker.ext.beans.*;

import org.apache.log4j.Logger;

import dart.server.Server;
import dart.server.Project;
import dart.server.wrap.*;


/**
*/


public class ZipServlet extends HttpServlet {
  // May not have an ivars, as the servlet engines are threaded

  // might not want to have a logger in the servlet
  static Logger logger = Logger.getLogger ( ZipServlet.class );

  void error ( HttpServletResponse res, String title, String msg ) throws IOException {
    PrintWriter out = res.getWriter();
    out.println( "<html><head><title>" + title + "</title></head>" );
    out.println( "<body>" );
    out.println( "<p>" );
    out.println( msg );
    out.println( "<p>" );
    out.println( "</body>" );
    out.println( "</html>" );
    return;
  }    

  public void doGet (HttpServletRequest req,
                     HttpServletResponse res)
    throws ServletException, IOException
  {
    // check the template name encoded in pathinfo and forward as necessary
    //
    String templateName = req.getPathInfo();

    if ( templateName.endsWith ( ".zip" ) ) {
      res.sendRedirect( req.getRequestURL() + "/index.html" );
      return;
    }

    logger.debug ( "Looking for " + templateName );

    // Try to open the zip file
    // try to split the name into *.zip / path

    String[] split = templateName.split ( ".zip" );
    String entryName = null, zipfile = null;
    if ( split.length == 1 ) {
      zipfile = split[0] + ".zip";
      entryName = "index.html";
    } else {
      zipfile = split[0] + ".zip";
      entryName = split[1].substring ( 1 );
    }

    String projectName = getInitParameter("project");
    Project project = Server.getProject( projectName );
    if ( project == null ) {
      error ( res, "Invalid project", "The project \"" + projectName + "\" was not found" );
      return;
    }

    // Open the zip file
    logger.debug ( "Opening: " + project.getDataDirectory().getPath() + zipfile  );
    ZipFile zip = new ZipFile ( project.getDataDirectory().getPath() + zipfile );

    if ( zip == null ) {
      error ( res, "Invalid zip", "Unable to open the zip file: " + zipfile );
      return;
    }


    ZipEntry entry = zip.getEntry ( entryName );
    logger.debug ( "Looking for: " + entryName );
    if ( entry == null ) {
      error ( res, "Invalid entry", "Unable to find " + entryName + " inside the zip file" );
      return;
    }


    String type = null;

    // setup the output
    String lower = entryName.toLowerCase();
    int idx = lower.lastIndexOf ( "." );
    if ( idx != -1 ) {
      String suffix = lower.substring ( idx );
      type = getInitParameter ( suffix );
    }

    if ( type == null ) { type = "text/html"; }
    logger.debug ( "Setting content type to " + type );
    res.setContentType ( type );

    if ( type.startsWith ( "text" ) ) {
      PrintWriter out = res.getWriter();
      Reader in = new BufferedReader ( new InputStreamReader ( zip.getInputStream ( entry ) ) );
      while ( true ) {
        int s = 1;
        char[] buffer = new char[2048];
        s = in.read ( buffer );
        if ( s == -1 ) {
          break;
        }
        out.write ( buffer, 0, s );
      }  
      out.close();
    } else {
      // Return binary
      OutputStream out = res.getOutputStream();
      InputStream in = new BufferedInputStream ( zip.getInputStream ( entry ) );
      while ( true ) {
        int s = 1;
        byte[] buffer = new byte[2048];
        s = in.read ( buffer );
        if ( s == -1 ) {
          break;
        }
        out.write ( buffer, 0, s );
      }  
      out.close();
    }
    return;
  }
}
