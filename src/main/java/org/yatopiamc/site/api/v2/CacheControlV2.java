package org.yatopiamc.site.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.v2.objects.BuildResult;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.objects.CommitV2;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class CacheControlV2 {

  private final LoadingCache<String, List<BuildV2>> latestBuilds =
      Caffeine.newBuilder().expireAfterWrite(Constants.CACHE_TIME).build(this::computeBuilds);

  public LoadingCache<String, List<BuildV2>> getLatestBuilds() {
    return latestBuilds;
  }

  public BuildV2 searchForBuild(String branch, int number) {
    List<BuildV2> builds = latestBuilds.get(branch);
    if (builds == null || builds.isEmpty()) {
      return new BuildV2(-1, "Branch or builds not found", null, null, null, null);
    }
    for (BuildV2 build : builds) {
      if (build.getNumber() == number) {
        return build;
      }
    }
    return new BuildV2(-1, "Branch or builds not found", null, null, null, null);
  }

  public BuildV2 getLatestSuccessfulBuild(String branch) {
    List<BuildV2> lastBuilds = latestBuilds.get(branch);
    if (lastBuilds == null || lastBuilds.isEmpty()) {
      return new BuildV2(-1, "Branch or builds not found", null, null, null, null);
    }
    for (BuildV2 build : lastBuilds) {
      if (build.getBuildResult() == BuildResult.SUCCESS) {
        return build;
      }
    }
    return new BuildV2(-1, "Branch or builds not found", null, null, null, null);
  }

  private List<BuildV2> computeBuilds(String branch) throws IOException {
    Call call =
        Constants.HTTP_CLIENT.newCall(
            new Request.Builder()
                .url(Constants.getJenkinsLastBuildsUrlFor(branch))
                .header("User-Agent", "api.yatopia.net")
                .build());
    try (Response response = call.execute()) {
      if (response.code() == 404) {
        return null;
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

          ret.add(
              new BuildV2(
                  number,
                  branch,
                  Constants.getJenkinsBuildDownloadUrlFor(branch, number),
                  jenkinsVisibilityUrl,
                  BuildResult.parse(nodeObject.get("result")),
                  commitsList.toArray(new CommitV2[0])));
        }
        return ret.isEmpty() ? null : ret;
      }
    }
  }
}
