package dart.server.wrapimpl;


import dart.server.wrap.*;
import net.sourceforge.jaxor.*;
import net.sourceforge.jaxor.mappers.*;


public class SubmissionImpl extends SubmissionBase {

  // Select a particular test
  public TestEntity selectTest ( String test ) {
    TestFinderBase finder = new TestFinderBase ( getJaxorContext() );
    try {
      return finder.selectByQualifiedNameAndSubmissionId ( test, getSubmissionId() );
    } catch ( Exception e ) {
      return null;
    }
  }

  // Select a particular test, as a list
  public TestList selectTestList ( String test ) {
    TestFinderBase finder = new TestFinderBase ( getJaxorContext() );
    try {
      return finder.selectByQualifiedNameAndSubmissionIdList ( test, getSubmissionId() );
    } catch ( Exception e ) {
      return null;
    }
  }

  // Select a root tests, as a list
  public TestList selectRootTestList () {
    TestFinderBase finder = new TestFinderBase ( getJaxorContext() );
    try {
      QueryParams q = new QueryParams();
      q.add ( getSubmissionId() );
      return finder.find ( "where SubmissionId = ? and ParentTestId is null", q );
    } catch ( Exception e ) {
      return null;
    }
  }

  // Select a particular test, using wildcards "%" in the test name
  public TestList selectTestListLike ( String test ) {
    TestFinderBase finder = new TestFinderBase ( getJaxorContext() );
    try {
      QueryParams q = new QueryParams();
      q.add(new net.sourceforge.jaxor.mappers.StringMapper(), test);
      q.add(new net.sourceforge.jaxor.mappers.IntegerMapper(), getSubmissionId());
      return finder.find("where qualifiedname like ? and submissionid = ?", q);
    } catch ( Exception e ) {
      return null;
    }
  }
  
  public SubmissionEntity getNextSubmission() {
    if ( getNextSubmissionId() == null ) {
      return null;
    }
    SubmissionFinderBase finder = new SubmissionFinderBase ( getJaxorContext() );
    return finder.selectBySubmissionId ( getNextSubmissionId() );
  }
  public SubmissionEntity getLastSubmission() {
    if ( getLastSubmissionId() == null ) {
      return null;
    }
    SubmissionFinderBase finder = new SubmissionFinderBase ( getJaxorContext() );
    return finder.selectBySubmissionId ( getLastSubmissionId() );
  }
    

  public SubmissionList selectLastSubmissions () {
    SubmissionFinderBase finder = new SubmissionFinderBase ( getJaxorContext() );
    try {
      QueryParams q = new QueryParams();
      q.add ( getClientId() );
      q.add ( getType() );
      q.add ( new TimestampMapper(), getTimeStamp() );
      return finder.find ( "where clientid = ? and type = ? and timestamp < ? order by timestamp desc", q );
    } catch ( Exception e ) {
      return null;
    }
  }

  public SubmissionList selectNextSubmissions () {
    SubmissionFinderBase finder = new SubmissionFinderBase ( getJaxorContext() );
    try {
      QueryParams q = new QueryParams();
      q.add ( getClientId() );
      q.add ( getType() );
      q.add ( new TimestampMapper(), getTimeStamp() );
      return finder.find ( "where clientid = ? and type = ? and timestamp > ? order by timestamp desc", q );
    } catch ( Exception e ) {
      return null;
    }
  }

  /** Get the site from the submission. 
   */
  public String getSite() {
    return getClientEntity().getSite();
  }

  /** Get the BuildName from the submission.
   */
  public String getBuildName() {
    return getClientEntity().getBuildName();
  }

  /** Get the number of updated files in the submission. Returns -1 if
   * there was update count available.
   */
  public Integer getUpdateCount() {
    try {
      TestList l = selectTestList( ".Update.Update" );

      if (l.size() == 1) {
        return l.get(0).getPassedSubTests();
      } else {
        return new Integer(-1); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Integer(-1);
    }
  }

  /** Return a boolean indicating whether the submission has an update
   * count.
   */
  public Boolean hasUpdateCount() {
    try {
      TestList l = selectTestList( ".Update.Update" );

      if (l.size() == 1) {
        return new Boolean(true);
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }

  /** Get the number of configuration errors in the
   * submission. Returns -1 if there was no config count measurement. 
   *
   * Config information has not been put in the database yet!!
   */
  public Integer getConfigCount() {
//    try {
//     TestList l = selectTestList( ".Update.Update" );

//     if (l.size() == 1) {
//       return l.get(0).getPassedSubTests();
//     } else {
//       return new Integer(-1); // Zero or large value?
//     }
//    }
//    catch (Exception e) {
//       return Integer(-1);
//    }    
    return new Integer(-1);
  }
  
  /** Return a boolean indicating whether the submission has a config
   * count.
   */
  public Boolean hasConfigCount() {
//    try {
//     TestList l = selectTestList( ".Update.Update" );

//     if (l.size() == 1) {
//       return new Boolean(true);
//     } else {
//       return new Boolean(false);
//     }
//   catch (Exception e) {
//     return new Boolean(false);
//   }
    return new Boolean(false);
  }
  
  /** Get the number of build errors in the submission. Returns -1 if
   * there was no build error count measurement.
   */
  public Integer getErrorCount() {
    try {
      TestList l = selectTestList( ".Build" );

      if (l.size() == 1) {
        Integer count = new Integer( l.get(0).selectResult( "ErrorCount" ).get(0).getValue() );
        return count;
      } else {
        return new Integer(-1); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Integer(-1);
    }
  }

  /** Return a boolean indicating whether the submission has an error
   * count.
   */
  public Boolean hasErrorCount() {
    try {
      TestList l = selectTestList( ".Build" );

      if (l.size() == 1) {
        ResultList lr = l.get(0).selectResult( "ErrorCount");
        if (lr.size() != 0) {
          return new Boolean(true);
        } else {
          return new Boolean(false);
        }
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }

  /** Get the number of build warnings in the submission. Returns -1
   * if there was no warning count measurement.
   */
  public Integer getWarningCount() {
    try {
      TestList l = selectTestList( ".Build" );

      if (l.size() == 1) {
        Integer count = new Integer( l.get(0).selectResult( "WarningCount" ).get(0).getValue() );
        return count;
      } else {
        return new Integer(-1); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Integer(-1);
    }
  }
  
  /** Return a boolean indicating whether the submission has a warning
   * count.
   */
  public Boolean hasWarningCount() {
    try {
      TestList l = selectTestList( ".Build" );

      if (l.size() == 1) {
        ResultList lr = l.get(0).selectResult( "WarningCount");
        if (lr.size() != 0) {
          return new Boolean(true);
        } else {
          return new Boolean(false);
        }
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }

  /** Get the number of passed tests in the submission. Returns -1 if
   * there was no passed test count available.
   */
  public Integer getPassedCount() {
    try {
      TestList l = selectTestList( ".Test" );

      if (l.size() == 1) {
        return l.get(0).getPassedSubTests();
      } else {
        return new Integer(-1); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Integer(-1);
    }
  }

  /** Return a boolean indicating whether the submission has a passed
   * count.
   */
  public Boolean hasPassedCount() {
    try {
      TestList l = selectTestList( ".Test" );

      if (l.size() == 1) {
        Integer count = l.get(0).getPassedSubTests();
        return new Boolean(true);
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }

  /** Get the number of failed tests in the submission. Returns -1 if
   * there was no failed test count available.
   */
  public Integer getFailedCount() {
    try {
      TestList l = selectTestList( ".Test" );
      
      if (l.size() == 1) {
        return l.get(0).getFailedSubTests();
      } else {
        return new Integer(-1); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Integer(-1);
    }
  }

  /** Return a boolean indicating whether the submission has a failed
   * count.
   */
  public Boolean hasFailedCount() {
    try {
      TestList l = selectTestList( ".Test" );

      if (l.size() == 1) {
        Integer count = l.get(0).getFailedSubTests();
        return new Boolean(true);
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }

  /** Get the number of notrun tests in the submission. Returns -1 if
   * there was no NotRun count available.
   */
  public Integer getNotRunCount() {
    try {
      TestList l = selectTestList( ".Test" );

      if (l.size() == 1) {
        return l.get(0).getNotRunSubTests();
      } else {
        return new Integer(-1); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Integer(-1);
    }
  }

  /** Return a boolean indicating whether the submission has a passed
   * count.
   */
  public Boolean hasNotRunCount() {
    try {
      TestList l = selectTestList( ".Test" );

      if (l.size() == 1) {
        Integer count = l.get(0).getNotRunSubTests();
        return new Boolean(true);
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }

  /** Get the elapsed time for the build. Returns -1 if there is no
   * elapsed time measurement.
   */
  public Float getElapsedBuildTime() {
    try {
      TestList l = selectTestList( ".Build" );
      
      if (l.size() == 1) {
        ResultList lr = l.get(0).selectResult("ElapsedTime");
        
        if (lr.size() > 0) {
          return new Float(lr.get(0).getValue());
        } else {
          return new Float(-1.0);  // Zero or large value?
        }
      } else {
        return new Float(-1.0); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Float(-1.0);
    }
  }

  /** Return a boolean indicating whether the submission has an
   * elapsed build time.
   */
  public Boolean hasElapsedBuildTime() {
    try {
      TestList l = selectTestList( ".Build" );

      if (l.size() == 1) {
        ResultList lr = l.get(0).selectResult("ElapsedTime");

        if (lr.size() > 0) {
          return new Boolean(true);
        } else {
          return new Boolean(false);
        }
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }

  /** Get the elapsed time for the tests. Returns -1 if there is no
   * elapsed time measurement.
   */
  public Float getElapsedTestTime() {
    try {
      TestList l = selectTestList( ".Test._Properties" );
      
      if (l.size() == 1) {
        ResultList lr = l.get(0).selectResult("ElapsedTime");
        
        if (lr.size() > 0) {
          return new Float(lr.get(0).getValue());
        } else {
          return new Float(-1.0);  // Zero or large value?
        }
      } else {
        return new Float(-1.0); // Zero or large value?
      }
    }
    catch (Exception e) {
      return new Float(-1.0);
    }
  }

  /** Return a boolean indicating whether the submission has an
   * elapsed test time.
   */
  public Boolean hasElapsedTestTime() {
    try {
      TestList l = selectTestList( ".Test._Properties" );

      if (l.size() == 1) {
        ResultList lr = l.get(0).selectResult("ElapsedTime");

        if (lr.size() > 0) {
          return new Boolean(true);
        } else {
          return new Boolean(false);
        }
      } else {
        return new Boolean(false);
      }
    }
    catch (Exception e) {
      return new Boolean(false);
    }
  }
}
