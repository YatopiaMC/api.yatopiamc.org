package org.yatopiamc.site.api.v1.objects;

import java.util.List;

public final class BuildV1 {

  private final int number, suiteId;
  private final Branch branch;
  private final List<Artifact> artifacts;

  public BuildV1(int number, int suiteId, Branch branch, List<Artifact> artifacts) {
    this.number = number;
    this.suiteId = suiteId;
    this.branch = branch;
    this.artifacts = artifacts;
  }

  public int getNumber() {
    return number;
  }

  public int getSuiteId() {
    return suiteId;
  }

  public Branch getBranch() {
    return branch;
  }

  public List<Artifact> getArtifacts() {
    return artifacts;
  }
}
