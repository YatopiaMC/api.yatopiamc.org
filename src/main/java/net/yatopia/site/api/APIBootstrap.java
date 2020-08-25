package net.yatopia.site.api;

import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.options;
import static spark.Spark.port;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.routes.LatestBuildRoute;
import net.yatopia.site.api.routes.LatestCommitRoute;
import net.yatopia.site.api.routes.OptionsHandler;
import net.yatopia.site.api.util.Constants;

public class APIBootstrap {

  public static void main(String[] args) {
    port(1696);
    initExceptionHandler(Throwable::printStackTrace);
    notFound(
        (request, response) -> {
          response.status(404);
          response.type("application/json");
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
          ObjectNode object = Constants.JSON_MAPPER.createObjectNode();
          object.put("error", 404);
          object.put("message", "Route not found");
          return object;
        });
    internalServerError(
        (request, response) -> {
          response.status(500);
          response.type("application/json");
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
          ObjectNode object = Constants.JSON_MAPPER.createObjectNode();
          object.put("error", 500);
          object.put("message", "Internal server error");
          return object;
        });

    OptionsHandler options = new OptionsHandler();
    CacheControl cacheControl = new CacheControl();

    get(
        "/",
        (request, response) -> {
          response.type("application/json");
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
          response.status(200);
          ObjectNode object = Constants.JSON_MAPPER.createObjectNode();
          object.put("status", "ONLINE");
          object.put("help", "at discord");
          object.put("forkMe", "https://github.com/YatopiaMC/api.yatopia.net/");
          return object;
        });
    options("/", options);

    get("/latestCommit", new LatestCommitRoute(cacheControl));
    options("/latestCommit", options);

    get("/latestBuild", new LatestBuildRoute(cacheControl));
    options("/latestBuild", options);  }
}
