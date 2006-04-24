package dart.server;



public class Version {
  // Contains Version info for Dart.
  public static final int MajorVersion = 0;
  public static final int MinorVersion = 8;
  public static final int PatchVersion = 5;
  public static String getVersionString() {
    return MajorVersion + "." + MinorVersion + "." + PatchVersion;
  }
  static public String getDBVersionString() {
    return Project.getDBVersionString();
  }
}
