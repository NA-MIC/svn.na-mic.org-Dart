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

    Connection connection = project.getConnection();
    JaxorContextImpl session = new JaxorContextImpl ( connection );
    TaskQueueFinderBase finder = new TaskQueueFinderBase ( session );
    CompletedTaskFinderBase completedTaskFinder = new CompletedTaskFinderBase ( session );
    QueryParams q = new QueryParams();
    q.add ( minPriority );
    q.add ( maxPriority );
    while ( true ) {
      TaskQueueList list = finder.find ( "where priority >= ? and priority <= ? order by priority, taskid", q );
      TaskQueueIterator i = list.iterator();
      if ( !i.hasNext() ) {
        logger.debug ( project.getTitle() + ": TaskQueue is empty" );
        break;
      }
      if ( tasks >= maxTasks && maxTasks != -1 ) {
        logger.debug ( project.getTitle() + ": Reached maximum tasks" ); 
        break;
      }
      tasks++;
      logger.debug ( project.getTitle() + ": Processing task " + tasks + ", remaining tasks " + list.size() );
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
        record = Boolean.valueOf ( subTaskProperties.getProperty ( "RecordCompletedTask", "true" ) ).booleanValue();
        // Try to find the object
        Task subtask = (Task) Class.forName ( task.getType() ).newInstance();
        // logger.debug ( project.getTitle() + ": Executing" );
        subtask.execute ( project, subTaskProperties );
        logger.debug ( project.getTitle() + ": Task completed" );
      } catch ( Exception e ) {
        // Log error, and go on to next task
        logger.error ( project.getTitle() + ": Failed to create or execute queued task", e );
        Status = "failed";
        Result = e.toString();
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
      }
    }
    connection.close();
  }
}
