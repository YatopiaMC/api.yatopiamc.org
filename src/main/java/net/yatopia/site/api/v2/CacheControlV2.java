package net.yatopia.site.api.v2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.v1.CacheControlV1;
import net.yatopia.site.api.v1.objects.Branch;
import net.yatopia.site.api.v2.objects.BuildV2;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class CacheControlV2 {

  private final CacheControlV1 v1;

  public CacheControlV2(CacheControlV1 v1) {
    this.v1 = v1;
  }

  private final LoadingCache<String, BuildV2> builds =
      Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).build(this::computeBuild);

  public LoadingCache<String, BuildV2> getBuilds() {
    return builds;
  }

  private BuildV2 computeBuild(String branch) throws IOException {
    Branch branchObj = v1.getBranches().get(branch);
    if (branchObj == null) {
      return new BuildV2(-1, new Branch("branch not found", null, false), null, null);
    }
    Call call =
        Constants.HTTP_CLIENT.newCall(
            new Request.Builder()
                .url(Constants.getJenkinsBuildsUrlFor(branch))
                .header("User-Agent", "api.yatopia.net")
                .build());
    try (Response response = call.execute()) {
      if (response.code() == 404) {
        return null;
      }
      try (InputStream in = response.body().byteStream()) {
        ObjectNode object = (ObjectNode) Constants.JSON_MAPPER.readTree(in);
        ObjectNode lastCompleted = (ObjectNode) object.get("lastCompletedBuild");
        int number = lastCompleted.get("number").asInt();
        String jenkinsVisibilityUrl = lastCompleted.get("url").asText();
        return new BuildV2(
            number,
            branchObj,
            Constants.getJenkinsBuildDownloadUrlFor(branch, number),
            jenkinsVisibilityUrl);
      }
    }
  }
}
