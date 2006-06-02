package dart.server.event;

import dart.server.event.*;
import dart.server.listener.*;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;
import java.text.*;
import java.util.Properties;

import org.apache.log4j.Logger;

public class MissingSubmissionEvent implements Event {
  static Logger logger = Logger.getLogger ( MissingSubmissionEvent.class );

  List ClientIds;
  Set UserIds;
  String TrackName;
  Long TrackId;

  public MissingSubmissionEvent ( List clientIds, Set userIds, String trackName, Long trackId ) {
    this.ClientIds = clientIds;
    this.UserIds = userIds;
    this.TrackName = trackName;
    this.TrackId = trackId;
  };
  public List getClientIds() { return ClientIds; }
  public Set getUserIds() { return UserIds; }
  public String getTrackName() { return TrackName; }
  public Long getTrackId() { return TrackId; }
}

