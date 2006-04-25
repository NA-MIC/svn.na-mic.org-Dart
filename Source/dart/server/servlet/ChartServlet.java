package dart.server.servlet;

import java.io.*;

import java.awt.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.http.*;
import javax.servlet.*;

import java.sql.Connection;
import net.sourceforge.jaxor.*;

import org.apache.log4j.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.date.MonthConstants;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;

import dart.server.Server;
import dart.server.Project;
import dart.server.StatusFormat;
import dart.server.wrap.*;


/**
 * Servlet for charting data. Query is passed in as parameters.
 *
 * Chart types:
 *    time - plot a measurement over time (various submissions).
 *         1) Single test over time for a single client/type
 *         2) Single test over time for multiple clients/type
 *         3) Multiple tests over time for single client/type
 *
 *    summary - plot of summary information
 *         1) Percentage of time spent in each test group
 *         2) Percentage of time spent build/test/etc.
 *         3) ???
 *
 * Url parameters:
 *    1) type - type of chart. must currently be "time". Defaults to "time".
 *    2) history - how many days back to look for
 *            measurements. Defaults to 1
 *    3) measurement - test result to plot.  Must be an item that can
 *            be cast to a float. Defaults to "Execution Time"
 *    4) title - chart title. Defaults to ""
 *    5) xlabel - title for x axis. Defaults to ""
 *    6) ylabel - title for y axis. Defaults to ""
 *    7) legend - hint for whether legend should be a test or
 *            submission (only needed when a single submission, single
 *            test is plotted. When there are multiple tests and
 *            single submission, submission should be the title and
 *            tests are in the legend. Wen there are multiple
 *            submissions and a single test, test should be the title
 *            and the submissions are in the legend.)
 *    8) width - plot width. Defaults to 400
 *    9) height - plot height. DEfaults to 300
*/


public class ChartServlet extends HttpServlet {
  // May not have an ivars, as the servlet engines are threaded

  // might not want to have a logger in the servlet
  static Logger logger = Logger.getLogger ( ChartServlet.class );

  void error ( PrintWriter out, String title, String msg, HashMap map ) {
    String projectName = (String) map.get ( "projectName" );
    out.println( "<html><head><title>" + title + "</title></head>" );
    out.println( "<body>" );
    out.println( "<h1>Chart for " );
    out.println( projectName );
    out.println( "</h1>" );
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
    // since servlets cannot have ivars, use a local variable (map) to
    // store information to pass to other methods of the servlet
    HashMap map = new HashMap();
    map.put ( "request", req );
    map.put ( "response", res );

    // setup the output
    OutputStream out = res.getOutputStream();
    map.put ( "out", out );

    // get the project name
    String projectName = getInitParameter("project");
    map.put ( "projectName", projectName );

    Project project;
    try {
      project = Server.getProject( projectName );
      if (project == null) {
        logger.debug( project.getTitle() + ": not found" );
        //        error ( out, "Dart Dashboard", "Dart: no project found matching \"" + projectName + "\"", map );
        out.close();
        return;
        }
    } catch ( Exception e ) {
      logger.debug( projectName + ": error getting project" );
      //      error ( out, "Dart Dashboard", "Dart: Error accessing project \"" + projectName + "\"", map );
      out.close();
      return;
    }
    map.put ( "project", project );

    // get the parameters from the url
    Map parameters = req.getParameterMap();
    String[] values;

    Connection connection = null;    
    try {
      JFreeChart chart = null;

      // Get the type of plot
      String type = "time";
      if (parameters.containsKey( "type" )) {
        values = (String[]) parameters.get( "type" );
        type = values[0];
      }

      // if a time plot, how far back in time should we go?
      int history = 1;
      if (type.equals("time")) {
        if (parameters.containsKey( "history" )) {
          values = (String[]) parameters.get( "history" );
          history = (new Integer(values[0])).intValue();
        }
      }

      // measurement to plot, default is "Execution Time"
      String measurement = "Execution Time";
      if (parameters.containsKey( "measurement" )) {
        values = (String[]) parameters.get( "measurement" );
        measurement = values[0];
      }
      
      // Get the titles for the plot
      String title = "";
      if (parameters.containsKey( "title" )) {
        values = (String[]) parameters.get( "title" );
        title = values[0];
      }
      String xlabel = "";
      if (parameters.containsKey( "xlabel" )) {
        values = (String[]) parameters.get( "xlabel" );
        xlabel = values[0];
      }
      String ylabel = "";
      if (parameters.containsKey( "ylabel" )) {
        values = (String[]) parameters.get( "ylabel" );
        ylabel = values[0];
      }
      String legend = "test";
      if (parameters.containsKey( "legend" )) {
        values = (String[]) parameters.get( "legend" );
        legend = values[0];
      }
        
      
      // Get the width and heights
      int width = 400;
      if (parameters.containsKey( "width" )) {
        values = (String[]) parameters.get( "width" );
        width = (new Integer(values[0])).intValue();
      }
      int height = 300;
      if (parameters.containsKey( "height" )) {
        values = (String[]) parameters.get( "height" );
        height = (new Integer(values[0])).intValue();
      }


      // Build the dataset to plot
      //
      //
      
      // connect to the database
      connection = project.getConnection();
      try {
        connection.setReadOnly ( true );
      } catch ( Exception e ) {
        logger.error ( project.getTitle()
         + ": Could not set connection to ReadOnly, possible security hole!", e );
      }
      JaxorContextImpl jaxorContext = new JaxorContextImpl ( connection );
      SubmissionFinderBase submissionFinder = new SubmissionFinderBase ( jaxorContext);
      ClientFinderBase clientFinder = new ClientFinderBase( jaxorContext );
      TestFinderBase testFinder = new TestFinderBase( jaxorContext );


      // Currently, we only know how to build time series charts of a
      // measurement (default execution time) over time
      if ( parameters.containsKey( "submissionid")
           && parameters.containsKey( "testname")) {
        
        // Define a times series collection to represent the dataset
        TimeSeriesCollection data = new TimeSeriesCollection();
       
        // get the submission (could change this to take several submissions)
        String [] submissionids = (String[])parameters.get( "submissionid" );

        Float minValue = new Float(Float.POSITIVE_INFINITY);
        Float maxValue = new Float(Float.NEGATIVE_INFINITY);
        
        for (int j=0; j < submissionids.length; ++j)
          {
            SubmissionEntity submission = submissionFinder
              .selectBySubmissionId(new Long(submissionids[j]));

            String submissionType = submission.getType();
            java.sql.Timestamp submissionTimeStamp = submission.getTimeStamp();

            // search back "history" days
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( submissionTimeStamp );
            calendar.add(Calendar.DAY_OF_MONTH, -history);
            
            java.sql.Timestamp lowerTimeStamp
              = new java.sql.Timestamp(calendar.getTimeInMillis());

            // logger.info("Getting data for between " + lowerTimeStamp + " and " + submissionTimeStamp );
        
            // get the client for this submission
            Long clientId = submission.getClientId();
            ClientEntity client = clientFinder.selectByClientId( clientId );
        
            // get the testname (could change this to take several testnames)
            String [] testnames = (String[])parameters.get( "testname" );

            for (int i=0; i < testnames.length; ++i) {
              String testname = testnames[i];

              // find all submissions of the appropriate type from this
              // client with this test before and including the timestamp of
              // the specified submission 
              QueryParams qp = new QueryParams();
              qp.add(new net.sourceforge.jaxor.mappers.IntegerMapper(), clientId);
              qp.add(new net.sourceforge.jaxor.mappers.StringMapper(),
                     submissionType);
              qp.add(new net.sourceforge.jaxor.mappers.TimestampMapper(),
                     submissionTimeStamp);
              qp.add(new net.sourceforge.jaxor.mappers.TimestampMapper(),
                     lowerTimeStamp );
              qp.add(new net.sourceforge.jaxor.mappers.StringMapper(), testname);

              TestList testList = testFinder.query("select Test.* from Test,Submission where Test.SubmissionId = Submission.SubmissionId and Submission.ClientId = ? and Submission.Type = ? and Submission.TimeStamp <= ? and Submission.TimeStamp >= ? and Test.QualifiedName = ? order by Submission.TimeStamp", qp);

              // Walk over the test list and build the dataset to
              // chart. The granuality of timestamps in Dart is to the
              // minute.
              String seriesTitle;
              if ((submissionids.length > 1) && (testnames.length > 1)) {
                // multiple submissions and multiple tests.  Need long
                // names to distinguish series
                seriesTitle = submission.getSite()
                  + "-" + submission.getBuildName()
                  + "-" + submissionType
                  + "-" + testname;
              } else if (submissionids.length > 1) {
                // only one test, client distinguishes the series
                seriesTitle = submission.getSite()
                  + "-" + submission.getBuildName()
                  + "-" + submissionType;
              } else if (testnames.length > 1) {
                // only one client, tests distinguish the series
                seriesTitle = testname;
              } else {
                // one tests, one client. use the "legend" hint
                if (legend.equals("test")) {
                  seriesTitle = testname;
                } else if (legend.equals("submission")) {
                  seriesTitle = submission.getSite()
                    + "-" + submission.getBuildName()
                    + "-" + submissionType;
                } else {
                  seriesTitle = testname;
                }
                
              }

              TimeSeries series = new TimeSeries(seriesTitle, Minute.class);
                    
              TestIterator it = testList.iterator();

              // This conditional is a hack.  Should come up with a
              // mechanism so that we can plot any result or any
              // information in a test.  Can I query this with
              // reflection? Or perhaps encapsulate these loops into a
              // function and override in a subclass.
              if ( measurement.equals("PassedSubTests") ) {
                while ( it.hasNext() )
                  {
                    TestEntity test = it.next();

                    java.sql.Timestamp testTimeStamp
                      = test.getSubmissionEntity().getTimeStamp();
                      
                    Minute time = new Minute( testTimeStamp );
                    series.add( time, test.getPassedSubTests() );
                  }
              } else if ( measurement.equals("FailedSubTests") ) {
                while ( it.hasNext() )
                  {
                    TestEntity test = it.next();

                    java.sql.Timestamp testTimeStamp
                      = test.getSubmissionEntity().getTimeStamp();
                      
                    Minute time = new Minute( testTimeStamp );
                    series.add( time, test.getFailedSubTests() );
                  }
              } else if ( measurement.equals("NotRunSubTests") ) {
                while ( it.hasNext() )
                  {
                    TestEntity test = it.next();

                    java.sql.Timestamp testTimeStamp
                      = test.getSubmissionEntity().getTimeStamp();
                      
                    Minute time = new Minute( testTimeStamp );
                    series.add( time, test.getNotRunSubTests() );
                  }
              } else if ( measurement.equals("Status") ) {
                while ( it.hasNext() )
                  {
                    TestEntity test = it.next();

                    java.sql.Timestamp testTimeStamp
                      = test.getSubmissionEntity().getTimeStamp();

                    Minute time = new Minute( testTimeStamp );
                    series.add( time, test.getStatusAsNumber() );
                  }
              } else {
                while ( it.hasNext() )
                  {
                    TestEntity test = it.next();
                    
                    ResultList results = test.selectResult( measurement );
                    
                    if (results.size() > 0) {
                      java.sql.Timestamp testTimeStamp
                        = test.getSubmissionEntity().getTimeStamp();
                      
                      Minute time = new Minute( testTimeStamp );
                      Float value = new Float(results.get(0).getValue());
                      series.add( time, value);
                      if (value.compareTo(minValue) < 0) {
                        minValue = value;
                      }
                      if (value.compareTo(maxValue) > 0) {
                        maxValue = value;
                      }
                    }
                  }
              }

              // add the series to dataset
              data.addSeries( series );
          
            } // end of testname loop
          } // end of submissionid loop
        
        // Create a chart of the appropriate type
        if (type.equals("time")) {
          // Create a time series chart.
          chart = ChartFactory.createTimeSeriesChart(title, xlabel, ylabel,
                                                     data, true, false, false);

          // Add shapes to the datapoints
          XYItemRenderer r = chart.getXYPlot().getRenderer();
          if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(false);
            // renderer.setSeriesShape(0, somecircleshape);
          }

          // format precision used on the y axis (use the default
          // number format for the locale)
          java.text.NumberFormat rangeFormat
            = java.text.NumberFormat.getNumberInstance();
          
          NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
          rangeAxis.setNumberFormatOverride( rangeFormat );
          
          // If the minValue and maxValue are between the same
          // integer, then JFreeChart doesn't do the right thing
          // when using a integer tick units. So set our own bounds
          // for the range.
          if (minValue.longValue() == maxValue.longValue()) {
            float fudge = 0.1f;
            rangeAxis.setAutoRange( false );
            rangeAxis.setLowerBound( minValue.floatValue() * (1.0 - fudge) );
            rangeAxis.setUpperBound( maxValue.floatValue() * (1.0 + fudge) );
          } else {
            // Use JFreeChart's integer range axis
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
          }
            
          // For plotting status, enforce the range to be [-1, 1]
          if (measurement.equals("Status")) {
            rangeAxis.setAutoRange( false );
            rangeAxis.setLowerBound( -1.1 );
            rangeAxis.setUpperBound( 1.1 );
            rangeAxis.setAutoTickUnitSelection( false );
            rangeAxis.setTickUnit( new NumberTickUnit(1.0) );
            
            StatusFormat statusFormat = new StatusFormat();
            rangeAxis.setNumberFormatOverride( statusFormat );
          }
        } else if (type.equals("bar")) {
          // create a bar chart
          chart = ChartFactory.createXYBarChart(title, xlabel, true, ylabel,
                                                data, PlotOrientation.VERTICAL,
                                                (data.getSeriesCount() > 1),
                                                false, false);
        } else if (type.equals("area")) {
          // create an area chart (does not use a date axis)
//           chart = ChartFactory.createXYAreaChart(title, xlabel, ylabel, data,
//                                                  PlotOrientation.VERTICAL, 
//                                                  (data.getSeriesCount() > 1),
//                                                  false, false);
          // hack
          chart = ChartFactory.createTimeSeriesChart(title, xlabel, ylabel,
                                                     data, true, false, false);
          XYBarRenderer rbar = new XYBarRenderer();
          XYStepAreaRenderer rsa = new XYStepAreaRenderer();
          chart.getXYPlot().setRenderer( rsa );

//           java.util.Date lower = new java.util.Date(105, 6, 1);
//           java.util.Date upper = new java.util.Date(105, 6, 15);
//           ((DateAxis)chart.getXYPlot().getDomainAxis()).setRange(lower, upper);
          
        } else if (type.equals("step")) {
          // create a step chart
          chart = ChartFactory.createXYStepChart(title, xlabel, ylabel, data,
                                                 PlotOrientation.VERTICAL, 
                                                 (data.getSeriesCount() > 1),
                                                 false, false);
        } 
        

        if (chart != null) {
          // Set the background to match other Dart measurements
          chart.setBackgroundPaint(new Color(0xb0, 0xc4, 0xde));
          
          // format to use for dates displayed on the x axis
          DateAxis axis = (DateAxis) chart.getXYPlot().getDomainAxis();
          axis.setDateFormatOverride(new SimpleDateFormat("MMM dd, yyyy"));
        }
      } // end of submissionid && testname

      // Pass the generated chart back through the server
      if (chart != null) {
        res.setContentType( "image/png" );
        ChartRenderingInfo info
          = new ChartRenderingInfo(new StandardEntityCollection() );
        ChartUtilities.writeChartAsPNG(out, chart, width, height, info);
      } else {
        logger.info("No chart created.");
      }

    }
    catch (Exception e) {
      logger.error("Exception caught: " + e, e);
//       PrintWriter pout = res.getWriter();
//       res.setContentType( "text/html" );
//       error(pout, "ChartServlet", "Unable to generate chart", map);
//       pout.close();
    }
    finally {
      if ( connection != null ) {
        try { connection.close(); } catch (Exception e) {};
      }
      out.close();
    }
  }


}
