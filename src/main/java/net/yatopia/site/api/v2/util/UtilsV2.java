package net.yatopia.site.api.v2.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.v1.util.UtilsV1;
import net.yatopia.site.api.v2.objects.BuildV2;

public class UtilsV2 {

  public static ObjectNode buildResponseNode(BuildV2 build) {
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    if (build.getBranch().getName().equalsIgnoreCase("branch not found")) {
      node.put("error", 404);
      node.put("message", "Invalid branch.");
      return node;
    }
    ObjectNode branch = Constants.JSON_MAPPER.createObjectNode();
    branch.put("name", build.getBranch().getName());
    branch.put("protected", build.getBranch().isProtected());
    branch.set("commit", UtilsV1.commitNode(build.getBranch().getLatestCommit()));

    node.set("branch", branch);
    node.put("number", build.getNumber());
    node.put("jenkinsViewUrl", build.getJenkinsVisilibityUrl());
    node.put("downloadUrl", getDownloadUrl(build));
    return node;
  }

  private static String getDownloadUrl(BuildV2 build) {
    return Constants.API_BASE_URL + "v2/latestBuild/download?branch=" + build.getBranch().getName();
  }
}
