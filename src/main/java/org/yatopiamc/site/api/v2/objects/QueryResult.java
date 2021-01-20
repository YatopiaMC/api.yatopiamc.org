package org.yatopiamc.site.api.v2.objects;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.Pair;

public enum QueryResult {
  BRANCH_NOT_FOUND("Branch not found", Collections.singletonList(HttpStatus.NOT_FOUND)),
  BUILD_NOT_FOUND("Build not found", Collections.singletonList(HttpStatus.NOT_FOUND)),
  BRANCH_HAS_NO_BUILDS(
      "This branch has no builds", Collections.singletonList(HttpStatus.NOT_FOUND)),
  CODEMC_DOWN(
      "CodeMC is currently offline.",
      Arrays.asList(HttpStatus.BAD_GATEWAY, HttpStatus.INTERNAL_SERVER_ERROR)),
  SUCCESS("success", Collections.singletonList(HttpStatus.OK)),
  INTERNAL_ERROR(
      "Internal error. builds == null",
      Collections.singletonList(HttpStatus.INTERNAL_SERVER_ERROR));

  public static boolean isFailure(QueryResult result) {
    if (result.getStatusCodes().size() != 1) {
      return true;
    }
    return result.getStatusCodes().get(0) != HttpStatus.OK;
  }

  public static Pair<QueryResult, HttpStatus> matchResult(HttpStatus statusCode) {
    for (QueryResult result : QueryResult.values()) {
      for (HttpStatus status : result.getStatusCodes()) {
        if (status == statusCode) {
          return Pair.of(result, statusCode);
        }
      }
    }
    return Pair.of(null, statusCode);
  }

  private final List<HttpStatus> statusCodes;
  private final String message;

  QueryResult(String message, List<HttpStatus> statusCodes) {
    this.statusCodes = statusCodes;
    this.message = message;
  }

  public List<HttpStatus> getStatusCodes() {
    return statusCodes;
  }

  public ObjectNode toJson(HttpStatus statusCode) {
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    node.put("error", statusCode.value());
    node.put("message", message);
    return node;
  }
}
