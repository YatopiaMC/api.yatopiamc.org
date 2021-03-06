package org.yatopiamc.site.api.v2.routes.download;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.Pair;
import org.yatopiamc.site.api.util.RateLimiter;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.CacheControlV2;
import org.yatopiamc.site.api.v2.objects.BuildQuery;
import org.yatopiamc.site.api.v2.objects.BuildResult;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.objects.QueryResult;
import spark.Request;
import spark.Response;
import spark.Route;

public class BuildDownloadRoute implements Route {

  private final CacheControlV2 cacheControl;

  public BuildDownloadRoute(CacheControlV2 cacheControl) {
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
    String branch = request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH);
    String build = request.params("build");
    int number;
    try {
      number = Integer.parseInt(build);
    } catch (NumberFormatException e) {
      response.status(400);
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 400);
      node.put("message", "Build number '" + build + "' is invalid.");
      node.put("note", "If you wanted to get the latest build, use /v2/latestBuild route");
      return node;
    }
    BuildQuery query = cacheControl.searchForBuild(branch, number);
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
    if (buildObj.getDownloadUrl() == null) {
      int status = buildObj.getBuildResult() == BuildResult.FAILURE ? 404 : 204;
      response.status(status);
      response.type("application/json");
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", status);
      String message =
          buildObj.getBuildResult() == BuildResult.FAILURE
              ? "Build resulted in failure, no artifacts present"
              : (buildObj.getBuildResult() == BuildResult.SUCCESS
                  ? "No artifacts present."
                  : "Build is currently building. No artifacts present.");
      node.put("message", message);
      return node;
    }
    response.redirect(buildObj.getDownloadUrl());
    return null;
  }
}
