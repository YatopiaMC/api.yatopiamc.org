package net.yatopia.site.api.v2.routes;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.util.RateLimiter;
import net.yatopia.site.api.util.Utils;
import net.yatopia.site.api.v2.CacheControlV2;
import net.yatopia.site.api.v2.objects.BuildV2;
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
      return Utils.rateLimitExceeded();
    }
    BuildV2 build =
        cacheControl
            .getBuilds()
            .get(request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH));
    if (build == null) {
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Invalid job.");
      return node;
    }
    response.redirect(build.getDownloadUrl());
    return null;
  }
}
