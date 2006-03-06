package dart.server.servlet;

import java.io.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.log4j.Logger;

import dart.server.*;
import dart.server.Project;

import org.apache.xmlrpc.*;

/**
   Servlet to accept submission data.
*/


public class CommandServlet extends HttpServlet {
  // May not have an ivars, as the servlet engines are threaded
  // might not want to have a logger in the servlet
  static Logger logger = Logger.getLogger ( CommandServlet.class );

  public void doPost (HttpServletRequest req,
                     HttpServletResponse res)
    throws ServletException, IOException
  {
    // setup the output
    res.setContentType( "text/xml" );

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
//     if ( container == null ) {
//       title = getInitParameter ( "qed" );
//       if ( title != null ) {
//         logger.debug ( "Looking for: " + title );
//         container = Server.getQED ( title );
//       }
//     } 
    if ( container == null ) {
      logger.error ( "Failed to find the associated container: " + title );
      return;
    }
    // Do the xmlrpc thing
    XmlRpcServer xmlrpc = container.getCommandManager().getXmlRpcServer();
    byte[] result = xmlrpc.execute ( req.getInputStream() );
    res.setContentLength ( result.length );
    OutputStream out = res.getOutputStream();
    out.write ( result );
    out.flush();
  }
}
