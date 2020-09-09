package net.yatopia.site.api.v1.objects;

public final class Branch {

  private final String name;
  private final Commit latestCommit;
  private final boolean isProtected;

  public Branch(String name, Commit latestCommit, boolean isProtected) {
    this.name = name;
    this.latestCommit = latestCommit;
    this.isProtected = isProtected;
  }

  public String getName() {
    return name;
  }

  public Commit getLatestCommit() {
    return latestCommit;
  }

  public boolean isProtected() {
    return isProtected;
  }
}
