package org.yatopiamc.site.api.v2.objects;

import org.springframework.http.HttpStatus;
import org.yatopiamc.site.api.util.Pair;

public class BuildQuery {

  public static BuildQuery sbQueryOf(BuildV2 build, Pair<QueryResult, HttpStatus> result) {
    return new BuildQuery(build, result);
  }

  private final BuildV2 build;
  private final Pair<QueryResult, HttpStatus> result;

  private BuildQuery(BuildV2 build, Pair<QueryResult, HttpStatus> result) {
    this.build = build;
    this.result = result;
  }

  public BuildV2 getBuild() {
    return build;
  }

  public Pair<QueryResult, HttpStatus> getResult() {
    return result;
  }
}
