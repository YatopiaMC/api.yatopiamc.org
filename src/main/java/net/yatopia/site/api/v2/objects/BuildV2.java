package net.yatopia.site.api.v2.objects;

import net.yatopia.site.api.v1.objects.Branch;

public final class BuildV2 {

  private final int number;
  private final Branch branch;
  private final String downloadUrl, jenkinsVisilibityUrl;

  public BuildV2(int number, Branch branch, String downloadUrl, String jenkinsVisilibityUrl) {
    this.number = number;
    this.branch = branch;
    this.downloadUrl = downloadUrl;
    this.jenkinsVisilibityUrl = jenkinsVisilibityUrl;
  }

  public int getNumber() {
    return number;
  }

  public Branch getBranch() {
    return branch;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public String getJenkinsVisilibityUrl() {
    return jenkinsVisilibityUrl;
  }
}
