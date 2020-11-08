package net.yatopia.site.api.util;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Utils {

  public static ObjectNode EMPTY_OBJECT = Constants.JSON_MAPPER.createObjectNode();

  private static ObjectNode rateLimitExceeded;

  public static ObjectNode rateLimitExceeded() {
    if (rateLimitExceeded != null) {
      return rateLimitExceeded;
    }
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    node.put("error", 429);
    node.put("message", "Rate limit exceeded.");
    rateLimitExceeded = node;
    return rateLimitExceeded;
  }
}
