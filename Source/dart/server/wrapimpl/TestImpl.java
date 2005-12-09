package dart.server.wrapimpl;

import dart.server.wrap.*;
import net.sourceforge.jaxor.*;
import net.sourceforge.jaxor.mappers.*;

public class TestImpl extends TestBase {
  Object defaultResult = null;
  
  /** Select all children of this Test, return a TestList
   */
  public TestList selectChildren () {
    TestFinderBase finder = new TestFinderBase ( getJaxorContext() );
    QueryParams params = new QueryParams();
    params.add(getTestId());

    // using find() instead of findUnique() avoids an exception if the
    // result is not in the table.  However, it forces us to return a
    // list of results instead of a single result.
    return finder.find("where ParentTestId=?", params);
  }

  /** Select the parent of this test
   */
  public TestEntity selectParent () {
    TestFinderBase finder = new TestFinderBase ( getJaxorContext() );
    QueryParams params = new QueryParams();
    params.add(getParentTestId());

    // using find() instead of findUnique() avoids an exception if the
    // result is not in the table.  However, it forces us to return a
    // list of results instead of a single result.
    try {
      return finder.findUnique("where TestId=?", params, false);
    } catch ( Exception e ) {
      return null;
    }
  }

  /** Select all children/grandchildren/... of this Test, return a TestList
   */
  public TestList selectAllChildren () {
    TestList all = new TestList();
    TestList children = selectChildren();

    TestIterator it = children.iterator();
    while (it.hasNext()) {
      TestEntity child = it.next();

      TestList grandChildren = child.selectAllChildren();

      // add child/grandChildren/... to master list
      all.add( child );
      TestIterator git = grandChildren.iterator();
      while (git.hasNext() ) {
        all.add( git.next() );
      }
    }

    return all;
  }

  /** Split the QualifiedName */
  public String[] splitQualifiedName () {
    return getQualifiedName().split ( "\\." );
  }

  /** Set the default value/object to return when a requested result does not
   * exist. */
  public void setDefaultResultValue(Object r) {
    defaultResult = r;
  }

  /** Get the default value to return when a requested result does not
   * exist. */
  public Object getDefaultResultValue() {
    return defaultResult;
  }
  

  /** Set/update an existing result
   */
  public void setResult ( String name, String type, String value ) {
    ResultFinderBase resultFinder = new ResultFinderBase ( getJaxorContext() );
    ResultEntity result;
    // See if we have already done this and update
    ResultList results;
    results = selectResult ( name );
    if ( results.toArray().length > 0 ) {
      result = results.toArray()[0];
    } else {
      result = resultFinder.newInstance();
      result.setTestId ( getTestId() );
      result.setType ( type );
      result.setName ( name );
    }
    result.setValue ( value );
  }
    


  /** Select a particular result, returns a (possibly empty)
   *  ResultList.
   */
  public ResultList selectResult ( final String result ) {
    ResultFinderBase finder = new ResultFinderBase ( getJaxorContext() );
    QueryParams params = new QueryParams();
    params.add(new net.sourceforge.jaxor.mappers.StringMapper(), result);
    params.add(getTestId());

    // using find() instead of findUnique() avoids an exception if the
    // result is not in the table.  However, it forces us to return a
    // list of results instead of a single result.
    return finder.find("where name=? and testId=?", params);
  }

  /** Generic "get" method that can be used by Freemarker to get
   * results that are in the result table and not in the test table.
   * This method makes a Test appear to include itself and all its results.
   */
  public Object get( String name ) {
    return getResultValueAsObject(name, defaultResult);
  }

  
  /** Select the value of a result with a default
   */
  public String getResultValue ( String name, String def ) {
    ResultFinderBase finder = new ResultFinderBase ( getJaxorContext() );
    QueryParams params = new QueryParams();
    params.add(new net.sourceforge.jaxor.mappers.StringMapper(), name);
    params.add(getTestId());

    // using find() instead of findUnique() avoids an exception if the
    // result is not in the table.  However, it forces us to return a
    // list of results instead of a single result.
   ResultList list = finder.find("where name=? and testId=?", params);
   if ( list.size() == 0 ) { return def; }
   return list.get(0).getValue();
  }    

  
  /** Select the value of a result as an object of the appropriate
   * type for that result.
   */
  public Object getResultValueAsObject ( String name, Object def ) {
    ResultFinderBase finder = new ResultFinderBase ( getJaxorContext() );
    QueryParams params = new QueryParams();
    params.add(new net.sourceforge.jaxor.mappers.StringMapper(),
               name);
    params.add(getTestId());

    // using find() instead of findUnique() avoids an exception if the
    // result is not in the table.  However, it forces us to return a
    // list of results instead of a single result.
   ResultList list = finder.find("where name=? and testId=?", params);
   if ( list.size() == 0 ) { return def; }

   ResultEntity result = list.get(0);
   String resultType = result.getType();
   if (resultType.equals("numeric/float")) {
     return new Float(result.getValue());
   } else if (resultType.equals("numeric/integer")) {
     return new Integer(result.getValue());
   } else if (resultType.equals("numeric/string")) {
     return new String( result.getValue() );
   }
   
   return list.get(0).getValue();
  }    


  /** Get the execution time result for this test.  
   */
  public Float getExecutionTime( ) {
    ResultList l;
    try {
      l = selectResult( "Execution Time" );
      // selectResult returns a ResultList.  If the list is non-empty,
      // return the value associated with the first item.  Expecting
      // only a single item in the list.  See documention of
      // selectResult() for more information.
      if (l.size() > 0) {
        return new Float(l.get(0).getValue());
      } else {
        return new Float(-1.0); // Zero or large value
      }
    }
    catch (Exception e) {
      return new Float(-1.0); // Zero or large value
    }
  }


  /** Get the completion status result for this test.  "defaultValue" is
   *  returned if no completion status result if found.
   */
  public String getCompletionStatus( String defaultValue ) {
    ResultList l;
    try {
      l = selectResult( "Completion Status" );
    
      // selectResult returns a ResultList.  If the list is non-empty,
      // return the value associated with the first item.  Expecting
      // only a single item in the list.  See documention of
      // selectResult() for more information.
      if (l.size() > 0) {
        return l.get(0).getValue();
      } else {
        return defaultValue;
      }
    }
    catch (Exception e) {
      return defaultValue;
    }
  }

  /** Get the completion status result for this test. Returns null if
   * there is no completion status recorded.
   */
  public String getCompletionStatus() {
    return getCompletionStatus( "" );
  }

  public String getStatusAsString() {
    String status = getStatus();

    if (status.equals("p")) {
      return "passed";
    } else if (status.equals("f")) {
      return "failed";
    } else if (status.equals("n")) {
      return "notrun";
    } else if (status.equals("m")) {
      return "meta";
    }

    return "meta";
  }


  /** Get the status of a test (pass, fail, notrun, meta) as an number */
  public Integer getStatusAsNumber() {
    String status = getStatus();

    if (status.equals("p")) {
      return new Integer(1);
    } else if (status.equals("f")) {
      return new Integer(0);
    } else if (status.equals("n")) {
      return new Integer(-1);
    } else if (status.equals("m")) {
      return new Integer(0);
    }

    return new Integer(0);
  }

}
