package dart;


import dart.server.Container;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.*;


public class ParseDateTimeStamp {

  public static void main ( String[] argv ) {
    
    String[] formats = Container.DateTimeFormats;
    Date date = null;
    for ( String stamp : argv ) {
      date = null;
      System.out.println ( stamp );
      for ( int idx = 0; idx < formats.length; idx++ ) {
        try {
          SimpleDateFormat parse = new SimpleDateFormat( formats[idx] );
          date = parse.parse ( stamp );
          if ( date != null ) {
            System.out.println ( "\tparsed with format " + formats[idx] );
          }
        } catch ( ParseException pe ) {
          System.out.println ( "\tfailed to parse with format " + formats[idx] );
          // Do nothing
        }
      }
    }
  }
}
