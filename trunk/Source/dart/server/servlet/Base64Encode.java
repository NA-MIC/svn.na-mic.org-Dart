package dart.server.servlet;


import dart.server.*;
import dart.server.util.*; 
import freemarker.core.*;
import freemarker.template.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
*/

public class Base64Encode implements TemplateMethodModel {
  Project project;
  public Base64Encode ( ) {
  }
  public Object exec ( List args ) throws TemplateModelException {
    if ( args.size() != 1 ) {
      throw new TemplateModelException ( "usage: <filename>" );
    }
    // Encode and return
    return Base64.encodeBytes ( ((String) args.get ( 0 )).getBytes() );
  }
}
