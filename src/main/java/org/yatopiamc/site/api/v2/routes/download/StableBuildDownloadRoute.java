package org.yatopiamc.site.api.v2.routes.download;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.StableBuildJSON;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.objects.BuildResult;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import spark.Request;
import spark.Response;
import spark.Route;

public class StableBuildDownloadRoute implements Route {

  private final StableBuildJSON stableBuildCache;

  public StableBuildDownloadRoute(StableBuildJSON stableBuildCache) {
    this.stableBuildCache = stableBuildCache;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
    if (!RateLimiter.canProceed(request.ip())) {
      response.status(429);
      response.type("application/json");
      return Utils.rateLimitExceeded();
    }
    String branch = request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH);
    int number = stableBuildCache.getStableBuild(branch);
    if (number == -1) {
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Stable build for branch \"" + branch + "\" not specified.");
      return node;
    }
    BuildV2 buildObj = stableBuildCache.getCacheControl().searchForBuild(branch, number);
    if (buildObj == null || buildObj.getBranch().equalsIgnoreCase("Branch or builds not found")) {
      response.status(404);
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Branch or builds not found");
      return node;
    }
    if (buildObj.getDownloadUrl() == null) {
      int status = buildObj.getBuildResult() == BuildResult.FAILURE ? 404 : 204;
      response.status(status);
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", status);
      String message =
          buildObj.getBuildResult() == BuildResult.FAILURE
              ? "Build resulted in failure, no artifacts present"
              : (buildObj.getBuildResult() == BuildResult.SUCCESS
                  ? "No artifacts present."
                  : "Build is currently building. No artifacts present.");
      node.put("message", message);
      return node;
    }
    response.redirect(buildObj.getDownloadUrl());
    return null;
  }
}
