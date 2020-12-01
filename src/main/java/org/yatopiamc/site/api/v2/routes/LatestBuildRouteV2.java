package org.yatopiamc.site.api.v2.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.CacheControlV2;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.util.UtilsV2;
import spark.Request;
import spark.Response;
import spark.Route;

public class LatestBuildRouteV2 implements Route {

  private final CacheControlV2 cacheControl;

  public LatestBuildRouteV2(CacheControlV2 cacheControl) {
    this.cacheControl = cacheControl;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    response.type("application/json");
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
    if (!RateLimiter.canProceed(request.ip())) {
      response.status(429);
      return Utils.rateLimitExceeded();
    }
    BuildV2 build =
        cacheControl.getLatestSuccessfulBuild(
            request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH));
    if (build == null) {
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Branch or builds not found");
      return node;
    }
    ObjectNode responseNode = UtilsV2.buildResponseNode(build, true);
    JsonNode error = responseNode.get("error");
    if (error != null) {
      response.status(error.asInt());
    } else {
      response.status(200);
    }
    return responseNode;
  }
}
