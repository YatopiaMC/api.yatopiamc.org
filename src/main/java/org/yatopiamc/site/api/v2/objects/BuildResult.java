package org.yatopiamc.site.api.v2.objects;

import com.fasterxml.jackson.databind.JsonNode;

public enum BuildResult {
  SUCCESS,
  FAILURE,
  BUILDING;

  public static BuildResult parse(JsonNode result) {
    if (result == null || result.asText() == null) {
      return BuildResult.BUILDING;
    }
    String resultText = result.asText();
    switch (resultText.toLowerCase()) {
      case "success":
        return BuildResult.SUCCESS;
      case "failure":
        return BuildResult.FAILURE;
      default:
        return BuildResult.BUILDING;
    }
  }
}
