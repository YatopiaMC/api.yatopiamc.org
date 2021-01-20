package org.yatopiamc.site.api.v2;

import static org.yatopiamc.site.api.v2.objects.BuildQuery.sbQueryOf;
import static org.yatopiamc.site.api.v2.objects.BuildsQuery.allQueryOf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.Pair;
import org.yatopiamc.site.api.v2.objects.BuildQuery;
import org.yatopiamc.site.api.v2.objects.BuildResult;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.objects.BuildsQuery;
import org.yatopiamc.site.api.v2.objects.CommitV2;
import org.yatopiamc.site.api.v2.objects.QueryResult;

public class CacheControlV2 {

  private final LoadingCache<String, BuildsQuery> latestBuilds =
      Caffeine.newBuilder().expireAfterWrite(Constants.CACHE_TIME).build(this::computeBuilds);

  public LoadingCache<String, BuildsQuery> getLatestBuilds() {
    return latestBuilds;
  }

  public BuildQuery searchForBuild(String branch, int number) {
    BuildsQuery builds = latestBuilds.get(branch);
    if (builds == null) {
      return sbQueryOf(null, Pair.of(QueryResult.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }
    Pair<QueryResult, HttpStatus> result = builds.getResult();
    if (result.left() != null && QueryResult.isFailure(result.left())) {
      return sbQueryOf(null, result);
    }
    if (result.left() == null) {
      return sbQueryOf(null, result);
    }
    for (BuildV2 build : builds.getBuilds()) {
      if (build.getNumber() == number) {
        return sbQueryOf(build, Pair.of(QueryResult.SUCCESS, HttpStatus.OK));
      }
    }
    return sbQueryOf(null, Pair.of(QueryResult.BUILD_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public BuildQuery getLatestSuccessfulBuild(String branch) {
    BuildsQuery lastBuilds = latestBuilds.get(branch);
    if (lastBuilds == null) {
      return sbQueryOf(null, Pair.of(QueryResult.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }
    Pair<QueryResult, HttpStatus> result = lastBuilds.getResult();
    if (result.left() != null && QueryResult.isFailure(result.left())) {
      return sbQueryOf(null, result);
    }
    if (result.left() == null) {
      return sbQueryOf(null, result);
    }
    for (BuildV2 build : lastBuilds.getBuilds()) {
      if (build.getBuildResult() == BuildResult.SUCCESS && build.getDownloadUrl() != null) {
        return sbQueryOf(build, Pair.of(QueryResult.SUCCESS, HttpStatus.OK));
      }
    }
    return sbQueryOf(null, Pair.of(QueryResult.BUILD_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  private BuildsQuery computeBuilds(String branch) throws IOException {
    Call call =
        Constants.HTTP_CLIENT.newCall(
            new Request.Builder()
                .url(Constants.getJenkinsLastBuildsUrlFor(branch))
                .header("User-Agent", "api.yatopia.net")
                .build());
    try (Response response = call.execute()) {
      if (!response.isSuccessful()) {
        if (response.code() == 404) {
          return allQueryOf(null, Pair.of(QueryResult.BRANCH_NOT_FOUND, HttpStatus.NOT_FOUND));
        }
        return allQueryOf(null, QueryResult.matchResult(HttpStatus.valueOf(response.code())));
      }
      try (InputStream in = response.body().byteStream()) {
        List<BuildV2> ret = new ArrayList<>();
        ObjectNode object = (ObjectNode) Constants.JSON_MAPPER.readTree(in);
        ArrayNode builds = (ArrayNode) object.get("builds");
        for (JsonNode node : builds) {
          if (!node.isObject()) {
            continue;
          }
          ObjectNode nodeObject = (ObjectNode) node;
          int number = nodeObject.get("number").asInt();
          String jenkinsVisibilityUrl = nodeObject.get("url").asText();
          if (!nodeObject.has("changeSets") || !nodeObject.get("changeSets").isArray()) {
            continue;
          }
          ArrayNode changeSets = (ArrayNode) nodeObject.get("changeSets");
          List<CommitV2> commitsList = new ArrayList<>();
          for (JsonNode changeSetNode : changeSets) {
            if (!changeSetNode.isObject()) {
              continue;
            }
            if (!changeSetNode.has("items") || !changeSetNode.get("items").isArray()) {
              continue;
            }
            ArrayNode items = (ArrayNode) changeSetNode.get("items");
            for (JsonNode commitNode : items) {
              if (!commitNode.isObject()) {
                continue;
              }
              commitsList.add(
                  new CommitV2(
                      commitNode.get("commitId").asText(),
                      commitNode.get("msg").asText(),
                      commitNode.get("date").asText(),
                      commitNode.get("comment").asText()));
            }
          }
          String artifactRelativePath = null;
          if (nodeObject.get("artifacts").isArray() && !nodeObject.get("artifacts").isEmpty()) {
            JsonNode artifact = nodeObject.get("artifacts").get(0);
            if (artifact.isObject() && !artifact.isEmpty()) {
              artifactRelativePath = artifact.get("relativePath").asText();
            }
          }

          ret.add(
              new BuildV2(
                  number,
                  branch,
                  Constants.getJenkinsBuildDownloadUrlFor(branch, number, artifactRelativePath),
                  jenkinsVisibilityUrl,
                  BuildResult.parse(nodeObject.get("result")),
                  commitsList.toArray(new CommitV2[0])));
        }
        return ret.isEmpty()
            ? allQueryOf(null, Pair.of(QueryResult.BRANCH_HAS_NO_BUILDS, HttpStatus.NOT_FOUND))
            : allQueryOf(ret, Pair.of(QueryResult.SUCCESS, HttpStatus.NOT_FOUND));
      }
    }
  }
}
