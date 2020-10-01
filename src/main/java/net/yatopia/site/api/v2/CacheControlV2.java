package net.yatopia.site.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.v2.objects.BuildV2;
import net.yatopia.site.api.v2.objects.CommitV2;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class CacheControlV2 {

  private final LoadingCache<String, BuildV2> latestBuilds =
      Caffeine.newBuilder().expireAfterWrite(Constants.CACHE_TIME).build(this::computeBuild);

  public LoadingCache<String, BuildV2> getLatestBuilds() {
    return latestBuilds;
  }

  private BuildV2 computeBuild(String branch) throws IOException {
    Call call =
        Constants.HTTP_CLIENT.newCall(
            new Request.Builder()
                .url(Constants.getJenkinsLatestBuildUrlFor(branch))
                .header("User-Agent", "api.yatopia.net")
                .build());
    try (Response response = call.execute()) {
      if (response.code() == 404) {
        return new BuildV2(-1, "Branch or builds not found", null, null, null);
      }
      try (InputStream in = response.body().byteStream()) {
        ObjectNode object = (ObjectNode) Constants.JSON_MAPPER.readTree(in);
        int number = object.get("number").asInt();
        String jenkinsVisibilityUrl = object.get("url").asText();
        if (object.get("changeSets").isArray() && object.get("changeSets").isEmpty()) {
          return new BuildV2(
              number,
              branch,
              Constants.getJenkinsBuildDownloadUrlFor(branch, number),
              jenkinsVisibilityUrl,
              null);
        }
        ObjectNode changeSets = (ObjectNode) object.get("changeSets").get(0).get("items").get(0);
        return new BuildV2(
            number,
            branch,
            Constants.getJenkinsBuildDownloadUrlFor(branch, number),
            jenkinsVisibilityUrl,
            new CommitV2(
                changeSets.get("commitId").asText(),
                changeSets.get("msg").asText(),
                changeSets.get("date").asText(),
                changeSets.get("comment").asText()));
      }
    }
  }

  private final LoadingCache<String, List<BuildV2>> latest10Builds =
      Caffeine.newBuilder().expireAfterWrite(Constants.CACHE_TIME).build(this::computeBuilds);

  public LoadingCache<String, List<BuildV2>> getLatest10Builds() {
    return latest10Builds;
  }

  public BuildV2 searchForBuild(String branch, int number) {
    List<BuildV2> builds = latest10Builds.get(branch);
    if (builds == null) {
      return new BuildV2(-1, "Branch or builds not found", null, null, null);
    }
    for (BuildV2 build : builds) {
      if (build.getNumber() == number) {
        return build;
      }
      if (build.getBranch().equalsIgnoreCase("Branch or builds not found")) {
        return build;
      }
    }
    return new BuildV2(-1, "Branch or builds not found", null, null, null);
  }

  private List<BuildV2> computeBuilds(String branch) throws IOException {
    Call call =
        Constants.HTTP_CLIENT.newCall(
            new Request.Builder()
                .url(Constants.getJenkinsLast10BuildsUrlFor(branch))
                .header("User-Agent", "api.yatopia.net")
                .build());
    try (Response response = call.execute()) {
      if (response.code() == 404) {
        return Collections.singletonList(
            new BuildV2(-1, "Branch or builds not found", null, null, null));
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
          if (nodeObject.get("changeSets").isArray() && nodeObject.get("changeSets").isEmpty()) {
            continue;
          }
          ObjectNode changeSets = (ObjectNode) nodeObject.get("changeSets").get(0).get("items").get(0);
          ret.add(
              new BuildV2(
                  number,
                  branch,
                  Constants.getJenkinsBuildDownloadUrlFor(branch, number),
                  jenkinsVisibilityUrl,
                  new CommitV2(
                      changeSets.get("commitId").asText(),
                      changeSets.get("msg").asText(),
                      changeSets.get("date").asText(),
                      changeSets.get("comment").asText())));
        }
        return ret;
      }
    }
  }
}
