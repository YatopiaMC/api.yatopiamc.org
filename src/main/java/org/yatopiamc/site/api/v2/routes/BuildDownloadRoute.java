package org.yatopiamc.site.api.v2.routes;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.CacheControlV2;
import org.yatopiamc.site.api.v2.objects.BuildResult;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import spark.Request;
import spark.Response;
import spark.Route;

public class BuildDownloadRoute implements Route {

  private final CacheControlV2 cacheControl;

  public BuildDownloadRoute(CacheControlV2 cacheControl) {
    this.cacheControl = cacheControl;
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
    String build = request.params("build");
    int number;
    try {
      number = Integer.parseInt(build);
    } catch (NumberFormatException e) {
      response.status(400);
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 400);
      node.put("message", "Build number '" + build + "' is invalid.");
      node.put("note", "If you wanted to get the latest build, use /v2/latestBuild route");
      return node;
    }
    BuildV2 buildObj = cacheControl.searchForBuild(branch, number);
    if (buildObj == null || buildObj.getBranch().equalsIgnoreCase("Branch or builds not found")) {
      response.status(404);
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Branch or builds not found");
      return node;
    }
    if (buildObj.getBuildResult() != BuildResult.SUCCESS) {
      int status = buildObj.getBuildResult() == BuildResult.FAILURE ? 404 : 204;
      response.status(status);
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", status);
      String message =
          buildObj.getBuildResult() == BuildResult.FAILURE
              ? "Build resulted in failure, no artifacts present"
              : "Build is currently building. No artifacts present.";
      node.put("message", message);
      return node;
    }
    response.redirect(buildObj.getDownloadUrl());
    return null;
  }
}
