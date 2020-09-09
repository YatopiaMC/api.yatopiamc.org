package net.yatopia.site.api.v1.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.yatopia.site.api.v1.objects.Artifact;
import net.yatopia.site.api.v1.objects.BuildV1;
import net.yatopia.site.api.v1.objects.Commit;
import net.yatopia.site.api.util.Constants;

public class UtilsV1 {

  public static ObjectNode buildResponseNode(BuildV1 build) {
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    if (build.getBranch().getName().equalsIgnoreCase("branch not found")) {
      node.put("error", 404);
      node.put("message", "Invalid branch.");
      return node;
    }
    if (build.getBranch().getName().equalsIgnoreCase("no successful build")) {
      node.put("error", 404);
      node.put("message", "Couldn't find a successful build for the specified branch.");
      return node;
    }

    ObjectNode branch = Constants.JSON_MAPPER.createObjectNode();
    branch.put("name", build.getBranch().getName());
    branch.put("protected", build.getBranch().isProtected());
    branch.set("commit", commitNode(build.getBranch().getLatestCommit()));

    node.set("branch", branch);
    node.put("number", build.getNumber());

    ArrayNode artifacts = Constants.JSON_MAPPER.createArrayNode();
    for (Artifact artifactObject : build.getArtifacts()) {
      ObjectNode artifact = Constants.JSON_MAPPER.createObjectNode();
      artifact.put("javaVersion", artifactObject.getJavaVersion());
      artifact.put("ghApiDownloadUrl", artifactObject.getApiDownloadUrl());
      artifact.put(
          "ghDownloadUrl",
          artifactObject.getGHDownloadUrl(Constants.GH_REPOSITORY, build.getSuiteId()));
      artifact.put(
          "comment",
          "GH does not allow unauthorized downloads. Automatic artifact download is not possible unless you do authorization.");
      artifacts.add(artifact);
    }

    node.set("artifacts", artifacts);
    return node;
  }

  public static ObjectNode commitNode(Commit commit) {
    ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
    node.put("sha", commit.getSha());
    node.put("authoredAt", commit.getTimestamp());
    node.put("message", commit.getMessage());

    ObjectNode author = Constants.JSON_MAPPER.createObjectNode();
    author.put("name", commit.getAuthorName());
    author.put("nickname", commit.getAuthorNickname());
    node.set("author", author);

    ObjectNode committer = Constants.JSON_MAPPER.createObjectNode();
    committer.put("name", commit.getCommitterName());
    committer.put("nickname", commit.getCommitterNickname());
    node.set("committer", committer);

    return node;
  }
}
