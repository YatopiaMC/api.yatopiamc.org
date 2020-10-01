package net.yatopia.site.api.v2.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.v2.objects.BuildV2;
import net.yatopia.site.api.v2.objects.CommitV2;

public class UtilsV2 {

  public static ObjectNode buildResponseNode(BuildV2 build) {
    return buildResponseNode(build, false);
  }

  public static ObjectNode buildResponseNode(BuildV2 build, boolean latest) {
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    if (build.getBranch().equalsIgnoreCase("Branch or builds not found")) {
      node.put("error", 404);
      node.put("message", "Branch or builds not found");
      return node;
    }
    ObjectNode branch = Constants.JSON_MAPPER.createObjectNode();
    branch.put("name", build.getBranch());
    branch.set("commit", commitNode(build.getCommit()));

    node.set("branch", branch);
    node.put("number", build.getNumber());
    node.put("jenkinsViewUrl", build.getJenkinsVisilibityUrl());
    node.put("downloadUrl", getDownloadUrl(build, latest));
    return node;
  }

  private static String getDownloadUrl(BuildV2 build, boolean latest) {
    if (latest) {
      return Constants.API_BASE_URL + "v2/latestBuild/download?branch=" + build.getBranch();
    }
    return Constants.API_BASE_URL
        + "v2/build/"
        + build.getNumber()
        + "/download?branch="
        + build.getBranch();
  }

  public static ObjectNode commitNode(CommitV2 commit) {
    if (commit == null) {
      return Constants.JSON_MAPPER.createObjectNode();
    }
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    node.put("sha", commit.getSha());
    node.put("authoredAt", commit.getTimestamp());
    node.put("message", commit.getMessage());
    node.put("comment", commit.getComment());
    return node;
  }
}
