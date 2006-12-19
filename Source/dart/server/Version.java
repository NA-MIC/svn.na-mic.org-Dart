package dart.server;



public class Version {
  // Contains Version info for Dart.
  public static final int MajorVersion = 1;
  public static final int MinorVersion = 0;
  public static final int PatchVersion = 8;
  public static String getVersionString() {
    return MajorVersion + "." + MinorVersion + "." + PatchVersion + ": $Id$";
  }
  static public String getDBVersionString() {
    return Project.getDBVersionString();
  }
}
