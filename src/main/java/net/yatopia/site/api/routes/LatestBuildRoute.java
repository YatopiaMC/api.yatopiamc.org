package net.yatopia.site.api.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.CacheControl;
import net.yatopia.site.api.objects.Build;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.util.RateLimiter;
import net.yatopia.site.api.util.Utils;
import spark.Request;
import spark.Response;
import spark.Route;

public class LatestBuildRoute implements Route {

  private final CacheControl cacheControl;

  public LatestBuildRoute(CacheControl cacheControl) {
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
    Build build =
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
    ObjectNode responseNode = Utils.buildResponseNode(build);
    JsonNode error = responseNode.get("error");
    if (error != null) {
      response.status(error.asInt());
    } else {
      response.status(200);
    }
    return responseNode;
  }
}
