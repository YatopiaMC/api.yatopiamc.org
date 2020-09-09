package net.yatopia.site.api.v1.routes;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.util.RateLimiter;
import net.yatopia.site.api.util.Utils;
import net.yatopia.site.api.v1.CacheControlV1;
import net.yatopia.site.api.v1.objects.Branch;
import net.yatopia.site.api.v1.util.UtilsV1;
import spark.Request;
import spark.Response;
import spark.Route;

public class LatestCommitRoute implements Route {

  private final CacheControlV1 cacheControl;

  public LatestCommitRoute(CacheControlV1 cacheControl) {
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
    String branchName = request.queryParamOrDefault("branch", Constants.DEFAULT_BRANCH);
    Branch branch = cacheControl.getBranches().get(branchName);
    if (branch == null) {
      response.status(404);
      ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
      node.put("error", 404);
      node.put("message", "Invalid branch.");
      return node;
    }
    response.status(200);
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    node.put("branch", branch.getName());
    node.put("protectedBranch", branch.isProtected());
    node.set("commit", UtilsV1.commitNode(branch.getLatestCommit()));
    return node;
  }
}
