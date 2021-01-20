package org.yatopiamc.site.api.v2.objects;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.yatopiamc.site.api.util.Pair;

public class BuildsQuery {

  public static BuildsQuery allQueryOf(List<BuildV2> builds, Pair<QueryResult, HttpStatus> result) {
    return new BuildsQuery(builds, result);
  }

  private final List<BuildV2> builds;
  private final Pair<QueryResult, HttpStatus> result;

  private BuildsQuery(List<BuildV2> builds, Pair<QueryResult, HttpStatus> result) {
    this.builds = builds;
    this.result = result;
  }

  public List<BuildV2> getBuilds() {
    return builds;
  }

  public Pair<QueryResult, HttpStatus> getResult() {
    return result;
  }
}
