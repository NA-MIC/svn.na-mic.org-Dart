package dart.server.task;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

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

    Connection connection = project.getConnection();
    TaskQueueResultSet i = null;
    try {
      JaxorContextImpl session = new JaxorContextImpl ( connection );
      TaskQueueFinderBase finder = new TaskQueueFinderBase ( session );
      CompletedTaskFinderBase completedTaskFinder = new CompletedTaskFinderBase ( session );
      QueryParams q = new QueryParams();
      q.add ( minPriority );
      q.add ( maxPriority );
      logger.info ( project.getTitle() + ": Finding tasks" );
      i = finder.findResultSet ( "where priority >= ? and priority <= ? order by priority, taskid", q );
      logger.info ( project.getTitle() + ": Found tasks" );
      while ( i.hasNext() ) {
        if ( tasks >= maxTasks && maxTasks != -1 ) {
          logger.debug ( project.getTitle() + ": Reached maximum tasks" ); 
          break;
        }
        tasks++;
        // logger.debug ( project.getTitle() + ": Processing task " + tasks + ", remaining tasks " + list.size() );
        String Status = "completed";
        String Result = "";

        // Find a task
        TaskQueueEntity task = i.next();
        logger.debug ( project.getTitle() + ": Found: " + task.getType() + " Priority: " + task.getPriority() + "\nProperties: " + task.getProperties() );
        Properties subTaskProperties;
        boolean record = true;      
        try {
          subTaskProperties = new Properties();
          subTaskProperties.load ( new ByteArrayInputStream ( task.getProperties().getBytes() ) );
          record = Boolean.valueOf ( subTaskProperties.getProperty ( "RecordCompletedTask", "false" ) ).booleanValue();
          // Try to find the object
          Task subtask = (Task) Class.forName ( task.getType() ).newInstance();
          logger.info ( project.getTitle() + ": Starting to execute task " + tasks + " " + task.getType() );
          subtask.execute ( project, subTaskProperties );
          logger.debug ( project.getTitle() + ": Task completed" );
        } catch ( Exception e ) {
          // Log error, and go on to next task
          logger.error ( project.getTitle() + ": Failed to create or execute queued task", e );
          Status = "failed";
          Result = e.toString();
          // Bomb out
          throw e;
        } finally {
          if ( record ) {
            CompletedTaskEntity CompletedTask = completedTaskFinder.newInstance( task.getTaskId() );
            CompletedTask.setPriority ( task.getPriority() );
            CompletedTask.setStatus ( Status );
            CompletedTask.setType ( task.getType() );
            CompletedTask.setProperties ( task.getProperties() );
            CompletedTask.setResult ( Result );
          }
          task.delete();
          session.commit();
          logger.info ( project.getTitle() + ": Processed task " + tasks + " " + task.getType() + ": " + Status );
            
        }
      }
    }
    catch (Throwable e) {
      logger.error ( project.getTitle() + ": Error in QueueManager", e );
    }
    finally {
      logger.debug("Closing connection.");
      // connection.close();
      project.closeConnection ( connection );
      if ( i != null ) {
        i.close();
      }
    }
    logger.info ( project.getTitle() + ": Finished processing tasks" );
  }
}
