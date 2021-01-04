package org.yatopiamc.site.api.v2.routes;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.StableBuildJSON;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.util.UtilsV2;
import spark.Request;
import spark.Response;
import spark.Route;

public class StableBuildRoute implements Route {

  private final StableBuildJSON stableBuildCache;

  public StableBuildRoute(StableBuildJSON stableBuildCache) {
    this.stableBuildCache = stableBuildCache;
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
    if (buildObj == null) {
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Invalid job.");
      return node;
    }
    ObjectNode ret = UtilsV2.buildResponseNode(buildObj);
    if (ret.has("error")) {
      response.status(ret.get("error").asInt());
    } else {
      response.status(200);
    }
    return ret;
  }
}
