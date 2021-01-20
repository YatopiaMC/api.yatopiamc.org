package org.yatopiamc.site.api.v2.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yatopiamc.site.api.util.Constants;
import org.yatopiamc.site.api.util.Utils;
import org.yatopiamc.site.api.v2.objects.BuildResult;
import org.yatopiamc.site.api.v2.objects.BuildV2;
import org.yatopiamc.site.api.v2.objects.CommitV2;

public class UtilsV2 {

  public static ObjectNode buildResponseNode(BuildV2 build) {
    return buildResponseNode(build, false, false);
  }

  public static ObjectNode buildResponseNode(BuildV2 build, boolean latest, boolean stable) {
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    ObjectNode branch = Constants.JSON_MAPPER.createObjectNode();
    branch.put("name", build.getBranch());
    if (build.getCommits().length >= 1) {
      branch.set("commit", commitNode(build.getCommits()[0]));
    } else {
      branch.set("commit", Utils.EMPTY_OBJECT);
    }

    node.set("branch", branch);

    ArrayNode changeSets = Constants.JSON_MAPPER.createArrayNode();
    if (build.getCommits().length >= 1) {
      for (CommitV2 commit : build.getCommits()) {
        changeSets.add(commitNode(commit));
      }
    }

    node.set("changeSets", changeSets);
    node.put("number", build.getNumber());
    node.put("jenkinsViewUrl", build.getJenkinsVisilibityUrl());
    node.put("status", build.getBuildResult().name());
    node.put(
        "downloadUrl",
        build.getBuildResult() == BuildResult.SUCCESS
            ? getDownloadUrl(build, latest, stable)
            : null);
    return node;
  }

  private static String getDownloadUrl(BuildV2 build, boolean latest, boolean stable) {
    if (latest) {
      return Constants.API_BASE_URL + "v2/latestBuild/download?branch=" + build.getBranch();
    }
    if (stable) {
      return Constants.API_BASE_URL + "v2/stableBuild/download?branch=" + build.getBranch();
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
