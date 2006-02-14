package dart.server.track;

import dart.server.*;
import dart.server.wrap.*;
import net.sourceforge.jaxor.*;

public interface Track {
  public String getName();
  public void setProject ( Project p );
  public boolean placeSubmission ( long submissionId );
  public long getTrackId ( java.sql.Timestamp ts );
  public boolean isValidTrack ( TrackEntity track );
}


