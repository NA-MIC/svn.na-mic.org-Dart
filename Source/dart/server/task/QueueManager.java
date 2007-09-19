package dart.server.task;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.Calendar;

import org.apache.log4j.Logger;

import dart.server.Project;
import net.sourceforge.jaxor.*;
import dart.server.wrap.*;

public class QueueManager implements Task {
  static Logger logger = Logger.getLogger ( QueueManager.class );   

  public QueueManager () {
  }
  public void execute ( Project project, Properties properties ) throws Exception {
    int maxTasks = -1, tasks = 0, minPriority = 0, maxPriority = 100;
    
    maxTasks = Integer.parseInt ( properties.getProperty ( "MaxTasks", "-1" ) );
    logger.info ( project.getTitle() + ": Starting to process tasks, MaxTasks is " + maxTasks );
    java.util.Date now = new java.util.Date();
    project.setStatistic ( "LastRollup", now.toString() );
    Connection connection = project.getConnection();
    TaskQueueResultSet i = null;
    try {
      /*
       * Note: the processing thread would unexpectedly die when using a TaskQueueList.
       * Apparently, moving to a result set fixed much of the problems.
       */
      JaxorContextImpl session = new JaxorContextImpl ( connection );
      TaskQueueFinderBase finder = new TaskQueueFinderBase ( session );
      CompletedTaskFinderBase completedTaskFinder = new CompletedTaskFinderBase ( session );
      QueryParams q = new QueryParams();
      q.add ( minPriority );
      q.add ( maxPriority );
      while ( true ) {
        logger.debug ( project.getTitle() + ": Finding tasks" );
        if ( i != null ) { i.close(); }
        i = finder.findResultSet ( "where priority >= ? and priority <= ? order by priority, taskid", q );
        logger.debug ( project.getTitle() + ": Found tasks" );
        if ( !i.hasNext() ) { break; }
        
        if ( tasks >= maxTasks && maxTasks != -1 ) {
          logger.debug ( project.getTitle() + ": Reached maximum tasks" ); 
          break;
        }
        tasks++;
        // logger.debug ( project.getTitle() + ": Processing task " + tasks + ", remaining tasks " + list.size() );
        String Status = "completed";
        String Result = "";
        
        // Find a task, close the ResultSet when we are done, so we don't hold the lock.
        // The query to fill the TaskQueueResultSet will be done on the next iteration.
        TaskQueueEntity task = i.next();
        i.close();

        logger.debug ( project.getTitle() + ": Found: " + task.getType() + " Priority: " + task.getPriority() + "\nProperties: " + task.getProperties() );
        Properties subTaskProperties;
        boolean record = true;      
        try {
          subTaskProperties = new Properties();
          subTaskProperties.load ( new ByteArrayInputStream ( task.getProperties().getBytes() ) );
          record = Boolean.valueOf ( subTaskProperties.getProperty ( "RecordCompletedTask", "false" ) ).booleanValue();
          // Try to find the object
          Task subtask = (Task) Class.forName ( task.getType() ).newInstance();
          logger.debug ( project.getTitle() + ": Starting to execute task " + tasks + " " + task.getType() );

          // Delete and commit, so we don't hold a lock on the TaskQueue table
          task.delete();
          session.commit();

          try {
            subtask.execute ( project, subTaskProperties );
          } catch ( Exception taskException ) {
            logger.warn ( project.getTitle() + ": Failed to create or execute queued task", taskException );
          }
          logger.debug ( project.getTitle() + ": Task completed" );
        } catch ( Exception e ) {
          // Log error, and go on to next task
          logger.error ( project.getTitle() + ": Failure in QueueManager", e );
          // Status = "failed";
          // Result = e.toString();
          // Bomb out
          throw e;
        } finally {
          /*
          if ( record ) {
            CompletedTaskEntity CompletedTask = completedTaskFinder.newInstance( task.getTaskId() );
            CompletedTask.setPriority ( task.getPriority() );
            CompletedTask.setStatus ( Status );
            CompletedTask.setType ( task.getType() );
            CompletedTask.setProperties ( task.getProperties() );
            CompletedTask.setResult ( Result );
          }
          */
          // task.delete();
          // session.commit();
          logger.debug ( project.log ( "Processed task " + tasks + " " + task.getType() + ": " + Status ) );
        }
      }
    }
    catch (Throwable e) {
      logger.error ( project.log ( "Error in QueueManager" ), e );
    }
    finally {
      logger.debug("Closing connection.");
      // connection.close();
      project.closeConnection ( connection );
      if ( i != null ) {
        i.close();
      }
    }
    logger.info ( project.log ( "Finished processing tasks" ) );
  }
}
