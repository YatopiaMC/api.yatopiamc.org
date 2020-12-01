package org.yatopiamc.site.api.routes;

import spark.Request;
import spark.Response;
import spark.Route;

public class OptionsHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws Exception {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
    response.header("Content-Length", "0");
    return null;
  }
}
