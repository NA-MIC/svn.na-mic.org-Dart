package dart.server.servlet;

import dart.server.*;
import dart.server.command.Submit;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.io.ByteArrayOutputStream;

import java.util.*;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 *  Allows a Dart server to accept submissions via HTTP PUT.
 *
 *  @author Zachary Mark
 *  @author zmark@cleversafe.com
 *
 *
 */

public class SubmitServlet extends HttpServlet {
  static Logger logger = Logger.getLogger ( Submit.class );   
  public void doPut( HttpServletRequest request, HttpServletResponse response ) throws
    ServletException, IOException {
    Container container = null;
     
    // see if we are a project
    String title = getInitParameter("project");
    if ( title != null ) {
      logger.debug ( "Looking for: " + title );
      container = Server.getProject( title );
    }
    if ( container == null ) {
      title = getInitParameter ( "server" );
      if ( title != null ) {
        logger.debug ( "Looking for: " + title );
        container = Server.getServer ( title );
      }
    } 

    ByteArrayOutputStream rawXML = new ByteArrayOutputStream();
    BufferedOutputStream out = new BufferedOutputStream(rawXML, request.getContentLength() + 2);
             
    InputStream r = new BufferedInputStream( request.getInputStream() );
      
    // Copy the PUT data into a byte array.
    byte[] dataBuf = new byte[request.getContentLength() + 1];
    while ( true ) {
      int bytesRead = 0;
      bytesRead = r.read ( dataBuf );

      if ( bytesRead == -1 ) {
        break;
      }
      out.write ( dataBuf, 0, bytesRead );
    }
    out.flush();
     
    // Give a dummy response.
    response.setContentType("text/plain");
    PrintWriter responseOut = response.getWriter();
    responseOut.println( "true" );
     
    try {
      Submit sub = new Submit(container, new Properties());
      sub.put(rawXML.toByteArray());
    }
    catch(Exception e) {
      logger.error("PUT submission failed!");
      return;
    }
    out.close();
  }
}

