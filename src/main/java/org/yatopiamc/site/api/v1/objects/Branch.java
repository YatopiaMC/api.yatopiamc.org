package org.yatopiamc.site.api.v1.objects;

public final class Branch {

  private final String name;
  private final CommitV1 latestCommit;
  private final boolean isProtected;

  public Branch(String name, CommitV1 latestCommit, boolean isProtected) {
    this.name = name;
    this.latestCommit = latestCommit;
    this.isProtected = isProtected;
  }

  public String getName() {
    return name;
  }

  public CommitV1 getLatestCommit() {
    return latestCommit;
  }

  public boolean isProtected() {
    return isProtected;
  }
}
