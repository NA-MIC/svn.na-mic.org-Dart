package dart.server.task;
import java.util.Properties;
import dart.server.Project;

public interface Task {
  public void execute ( Project project, Properties properties ) throws Exception;
}

