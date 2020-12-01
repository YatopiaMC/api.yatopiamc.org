package org.yatopiamc.site.api.v2.routes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.CacheControlV2;
import org.yatopiamc.site.api.v2.objects.BuildResult;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.util.UtilsV2;
import spark.Request;
import spark.Response;
import spark.Route;

public class BuildsRoute implements Route {

  private final CacheControlV2 cacheControl;

  public BuildsRoute(CacheControlV2 cacheControl) {
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
    List<BuildV2> builds = cacheControl.getLatestBuilds().get(branch);
    if (builds == null || builds.size() == 0) {
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Branch or builds not found");
      return node;
    }
    boolean onlySuccessful =
        Boolean.parseBoolean(request.queryParamOrDefault("onlySuccessful", "false"));
    ObjectNode ret = Constants.JSON_MAPPER.createObjectNode();
    ArrayNode buildsNode = Constants.JSON_MAPPER.createArrayNode();
    for (BuildV2 build : builds) {
      if (onlySuccessful && build.getBuildResult() != BuildResult.SUCCESS) {
        continue;
      }
      ObjectNode node = UtilsV2.buildResponseNode(build);
      buildsNode.add(node);
    }
    response.status(200);
    ret.set("builds", buildsNode);
    return ret;
  }
}
