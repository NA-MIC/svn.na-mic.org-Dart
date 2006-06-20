package dart.server.task;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import dart.server.Project;

public class ScheduledTask implements StatefulJob {
  static Logger logger = Logger.getLogger ( ScheduledTask.class );   

  public ScheduledTask () {
  }
  public void execute ( JobExecutionContext context ) throws JobExecutionException {
    logger.debug ( "executing: " + context.getJobDetail().getName() );
    JobDataMap map = context.getJobDetail().getJobDataMap();
    Project project = null;
    Properties properties = null;
    Task task = null;
    try {
      project = (Project) map.get ( "Project" );
      properties = (Properties) map.get ( "Properties" );
      Class taskClass = Class.forName ( map.getString ( "Type" ) );
      task = (Task)taskClass.newInstance();
    } catch ( Exception e ) {
      logger.error ( "Did not find project or properties in job context", e );
      throw new JobExecutionException ( new Exception ( "Did not find project or properties in job context", e ) );
    }

    try {
      logger.debug ( "Starting task" );
      task.execute ( project, properties );
    } catch ( Throwable e ) {
      logger.error ( project.getTitle() + ": Execution failed: " + context.getJobDetail().getName(), e );
      throw new JobExecutionException ( new Exception ( "Execution failed: " + context.getJobDetail().getName(), e ) );
    }
  }
}
