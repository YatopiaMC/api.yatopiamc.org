package net.yatopia.site.api;

import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.options;
import static spark.Spark.port;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.routes.OptionsHandler;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.v1.CacheControlV1;
import net.yatopia.site.api.v1.routes.LatestBuildRouteV1;
import net.yatopia.site.api.v1.routes.LatestCommitRoute;
import net.yatopia.site.api.v2.CacheControlV2;
import net.yatopia.site.api.v2.routes.LatestBuildDownloadRoute;
import net.yatopia.site.api.v2.routes.LatestBuildRouteV2;

public class APIBootstrap {

  private static ObjectNode cached404;
  private static ObjectNode cached500;
  private static ObjectNode cachedDefault;

  public static void main(String[] args) {
    port(1696);
    initExceptionHandler(Throwable::printStackTrace);
    notFound(
        (request, response) -> {
          response.status(404);
          response.type("application/json");
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
          if (cached404 != null) {
            return cached404;
          }
          ObjectNode object = Constants.JSON_MAPPER.createObjectNode();
          object.put("error", 404);
          object.put("message", "Route not found");
          APIBootstrap.cached404 = object;
          return cached404;
        });
    internalServerError(
        (request, response) -> {
          response.status(500);
          response.type("application/json");
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
          if (cached500 != null) {
            return cached500;
          }
          ObjectNode object = Constants.JSON_MAPPER.createObjectNode();
          object.put("error", 500);
          object.put("message", "Internal server error");
          APIBootstrap.cached500 = object;
          return cached500;
        });

    OptionsHandler options = new OptionsHandler();
    CacheControlV1 cacheControlV1 = new CacheControlV1();

    get(
        "/",
        (request, response) -> {
          response.type("application/json");
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
          response.status(200);
          if (cachedDefault != null) {
            return cachedDefault;
          }
          ObjectNode object = Constants.JSON_MAPPER.createObjectNode();
          object.put("status", "ONLINE");
          object.put("help", "at discord");
          object.put("forkMe", "https://github.com/YatopiaMC/api.yatopia.net/");
          APIBootstrap.cachedDefault = object;
          return cachedDefault;
        });
    options("/", options);

    get("/latestCommit", new LatestCommitRoute(cacheControlV1));
    options("/latestCommit", options);

    get("/latestBuild", new LatestBuildRouteV1(cacheControlV1));
    options("/latestBuild", options);

    // v2
    CacheControlV2 cacheControlV2 = new CacheControlV2(cacheControlV1);

    get("/v2/latestBuild", new LatestBuildRouteV2(cacheControlV2));
    options("/v2/latestBuild", options);

    get("/v2/latestBuild/download", new LatestBuildDownloadRoute(cacheControlV2));
    options("/v2/latestBuild/download", options);
  }
}
