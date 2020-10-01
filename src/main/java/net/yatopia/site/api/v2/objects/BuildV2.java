package net.yatopia.site.api.v2.objects;

public final class BuildV2 {

  private final int number;
  private final String branch, downloadUrl, jenkinsVisilibityUrl;
  private final CommitV2 commit;

  public BuildV2(
      int number, String branch, String downloadUrl, String jenkinsVisilibityUrl, CommitV2 commit) {
    this.number = number;
    this.branch = branch;
    this.downloadUrl = downloadUrl;
    this.jenkinsVisilibityUrl = jenkinsVisilibityUrl;
    this.commit = commit;
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

  public CommitV2 getCommit() {
    return commit;
  }
}
