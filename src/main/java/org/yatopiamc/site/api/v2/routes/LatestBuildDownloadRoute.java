package org.yatopiamc.site.api.v2.routes;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.CacheControlV2;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import spark.Request;
import spark.Response;
import spark.Route;

public class LatestBuildDownloadRoute implements Route {

  private final CacheControlV2 cacheControl;

  public LatestBuildDownloadRoute(CacheControlV2 cacheControl) {
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
    BuildV2 build =
        cacheControl.getLatestSuccessfulBuild(
            request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH));
    if (build == null || build.getBranch().equalsIgnoreCase("Branch or builds not found")) {
      response.type("application/json");
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Branch or builds not found");
      return node;
    }
    response.redirect(build.getDownloadUrl());
    return null;
  }
}
