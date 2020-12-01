package org.yatopiamc.site.api.v2.objects;

public final class BuildV2 {

  private final int number;
  private final String branch, downloadUrl, jenkinsVisilibityUrl;
  private final CommitV2[] commits;
  private final BuildResult buildResult;

  public BuildV2(
      int number,
      String branch,
      String downloadUrl,
      String jenkinsVisilibityUrl,
      BuildResult buildResult,
      CommitV2[] commits) {
    this.number = number;
    this.branch = branch;
    this.downloadUrl = downloadUrl;
    this.jenkinsVisilibityUrl = jenkinsVisilibityUrl;
    this.buildResult = buildResult;
    this.commits = commits;
  }

  public int getNumber() {
    return number;
  }

  public String getBranch() {
    return branch;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public String getJenkinsVisilibityUrl() {
    return jenkinsVisilibityUrl;
  }

  public BuildResult getBuildResult() {
    return buildResult;
  }

  public CommitV2[] getCommits() {
    return commits;
  }
}
