package org.yatopiamc.site.api.v1.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v1.CacheControlV1;
import org.yatopiamc.site.api.v1.objects.BuildV1;
import org.yatopiamc.site.api.v1.util.UtilsV1;
import spark.Request;
import spark.Response;
import spark.Route;

public class LatestBuildRouteV1 implements Route {

  private final CacheControlV1 cacheControl;

  public LatestBuildRouteV1(CacheControlV1 cacheControl) {
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
    BuildV1 build =
        cacheControl
            .getBuilds()
            .get(request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH));
    if (build == null) {
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "GH returned non 200 code whilst trying to get the latest build.");
      return node;
    }
    ObjectNode responseNode = UtilsV1.buildResponseNode(build);
    JsonNode error = responseNode.get("error");
    if (error != null) {
      response.status(error.asInt());
    } else {
      response.status(200);
    }
    return responseNode;
  }
}
