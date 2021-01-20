package org.yatopiamc.site.api.v2.routes.download;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.Pair;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.CacheControlV2;
import org.yatopiamc.site.api.v2.objects.BuildQuery;
import org.yatopiamc.site.api.v2.objects.QueryResult;
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
    BuildQuery query =
        cacheControl.getLatestSuccessfulBuild(
            request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH));
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
    response.redirect(query.getBuild().getDownloadUrl());
    return null;
  }
}
