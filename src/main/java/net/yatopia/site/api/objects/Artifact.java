package net.yatopia.site.api.objects;

public final class Artifact {

  private final int id;
  private final String apiDownloadUrl, javaVersion;

  public Artifact(int id, String apiDownloadUrl, String javaVersion) {
    this.id = id;
    this.apiDownloadUrl = apiDownloadUrl;
    this.javaVersion = javaVersion;
  }

  public int getId() {
    return id;
  }

  public String getApiDownloadUrl() {
    return apiDownloadUrl;
  }

  public String getJavaVersion() {
    return javaVersion;
  }

  public String getGHDownloadUrl(String repository, int suiteId) {
    return "https://github.com/" + repository + "/suites/" + suiteId + "/artifacts/" + id;
  }
}
