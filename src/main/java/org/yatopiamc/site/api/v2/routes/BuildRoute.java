package org.yatopiamc.site.api.v2.routes;

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

public class BuildRoute implements Route {

  private final CacheControlV2 cacheControl;

  public BuildRoute(CacheControlV2 cacheControl) {
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
    String branch = request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH);
    String build = request.params("build");
    int number;
    try {
      number = Integer.parseInt(build);
    } catch (NumberFormatException e) {
      response.status(400);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 400);
      node.put("message", "Build number '" + build + "' is invalid.");
      node.put("note", "If you wanted to get the latest build, use /v2/latestBuild route");
      return node;
    }
    BuildV2 buildObj = cacheControl.searchForBuild(branch, number);
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
