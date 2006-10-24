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

public class UpdateEvent implements Event {
  static Logger logger = Logger.getLogger ( UpdateEvent.class );

  long SubmissionId;
  public UpdateEvent ( long id ) { this.SubmissionId = id; };
  public long getSubmissionId() { return SubmissionId; }
}

