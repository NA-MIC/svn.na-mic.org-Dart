package dart.server.track;

import dart.server.*;
import dart.server.wrap.*;
import net.sourceforge.jaxor.*;

public interface Track {
  public String getName();
  public void setProject ( Project p );
  public boolean placeSubmission ( int submissionId );
  public int getTrackId ( java.sql.Timestamp ts );
}


