package org.yatopiamc.site.api.v2.routes;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.Pair;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.StableBuildJSON;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.objects.BuildQuery;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.objects.QueryResult;
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
    BuildQuery query = stableBuildCache.getCacheControl().searchForBuild(branch, number);
    Pair<QueryResult, HttpStatus> result = query.getResult();
    if (result.left() != null && QueryResult.isFailure(result.left())) {
      response.status(result.right().value());
      response.type("application/json");
      return result.left().toJson(result.right());
    }
    if (result.left() == null && result.right() != HttpStatus.OK) {
      response.status(result.right().value());
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", result.right().value());
      node.put("message", result.right().getReasonPhrase());
      return node;
    }
    BuildV2 buildObj = query.getBuild();
    response.status(200);
    return UtilsV2.buildResponseNode(buildObj, false, true);
  }
}
