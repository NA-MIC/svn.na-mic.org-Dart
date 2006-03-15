package dart.server.track;

import dart.server.*;
import dart.server.wrap.*;
import net.sourceforge.jaxor.*;

public interface Track {
  public static final long DEFAULT_PRIORITY=100;

  public String getName();
  public void setProject ( Project p );
  public boolean placeSubmission ( long submissionId );
  public long getTrackId ( java.sql.Timestamp ts );
  public boolean isValidTrack ( TrackEntity track );
  public void setPriority( String priority );
  public void setPriority( long priority );
  public long getPriority();

}


