/*
 * Notice: this code is a modified version of PropertyHandlerMapping.java from the 
 * XMLRPC 3.0a1 release.  As such, it contains the Apache copyright, though
 * the contents may have significantly changed.
 */

/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dart.server.command;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.*;
import org.apache.xmlrpc.*;


/** A handler mapping based on a property file. The property file
 * contains a set of properties. The property key is taken as the
 * handler name. The property value is taken as the name of a
 * class being instantiated. For any non-void, non-static, and
 * public method in the class, an entry in the handler map is
 * generated.<br>
 * The following constrains apply to the classes:
 * <ol>
 *   <li>The classes must be stateless. In other words, any
 *     instance of the class must be completely thread safe.</li>
 * </ol>
 * A typical use would be, to specify interface names as the
 * property keys and implementations as the values.
 */
public class HandlerMapping implements XmlRpcHandlerMapping {
  private final HashMap map = new HashMap();

  public void addHandler ( String key, Object command ) throws IOException, XmlRpcException {
    final Object o;
    o = command;
    final Class c = o.getClass();
    Method[] methods = c.getMethods();
    for (int i = 0;  i < methods.length;  i++) {
      final Method method = methods[i];
      if (!Modifier.isPublic(method.getModifiers())) {
        continue;  // Ignore methods, which aren't public
      }
      if (Modifier.isStatic(method.getModifiers())) {
        continue;  // Ignore methods, which are static
      }
      if (method.getReturnType() == void.class) {
        continue;  // Ignore void methods.
      }
      if (method.getDeclaringClass() == Object.class) {
        continue;  // Ignore methods from Object.class
      }
      String name = key + "." + method.getName();
      if (!map.containsKey(name)) {
        map.put(name, new XmlRpcHandler(){
            public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
              Object[] args = new Object[pRequest.getParameterCount()];
              for (int j = 0;  j < args.length;  j++) {
                args[j] = pRequest.getParameter(j);
              }
              try {
                return method.invoke(o, args);
              } catch (IllegalAccessException e) {
                throw new XmlRpcException("Illegal access to method "
                                          + method.getName() + " in class "
                                          + c.getName(), e);
              } catch (IllegalArgumentException e) {
                throw new XmlRpcException("Illegal argument for method "
                                          + method.getName() + " in class "
                                          + c.getName(), e);
              } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                throw new XmlRpcException("Failed to invoke method "
                                          + method.getName() + " in class "
                                          + c.getName() + ": "
                                          + t.getMessage(), t);
              }
            }
          });
      }
    }
  }
  public XmlRpcHandler getHandler(String handlerName)
    throws XmlRpcNoSuchHandlerException, XmlRpcException {
    XmlRpcHandler result = (XmlRpcHandler) map.get(handlerName);
    if (result == null) {
      throw new XmlRpcNoSuchHandlerException("No such handler: " + handlerName);
    }
    return result;
  }
}

