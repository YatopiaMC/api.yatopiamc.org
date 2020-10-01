package net.yatopia.site.api.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.yatopia.site.api.util.Constants;
import net.yatopia.site.api.v1.objects.Artifact;
import net.yatopia.site.api.v1.objects.Branch;
import net.yatopia.site.api.v1.objects.BuildV1;
import net.yatopia.site.api.v1.objects.CommitV1;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class CacheControlV1 {

  private LoadingCache<String, Branch> branches =
      Caffeine.newBuilder().expireAfterWrite(Constants.CACHE_TIME).build(this::computeBranch);

  public LoadingCache<String, Branch> getBranches() {
    return branches;
  }

  private Branch computeBranch(String name) throws IOException {
    Call call =
        Constants.HTTP_CLIENT.newCall(
            new Request.Builder()
                .addHeader("User-Agent", "api.yatopia.net")
                .url(String.format(Constants.GITHUB_API_BRANCHES, name))
                .build());
    try (Response response = call.execute()) {
      if (response.code() != 200) {
        return null;
      }
      try (InputStream in = response.body().byteStream()) {
        ObjectNode object = (ObjectNode) Constants.JSON_MAPPER.readTree(in);
        String commitSha = object.get("commit").get("sha").asText();
        String authorName = object.get("commit").get("commit").get("author").get("name").asText();
        String authorNickname = object.get("commit").get("author").get("login").asText();
        String committerName =
            object.get("commit").get("commit").get("committer").get("name").asText();
        String committerNickname = object.get("commit").get("committer").get("login").asText();
        String message = object.get("commit").get("commit").get("message").asText();
        String timestamp = object.get("commit").get("commit").get("author").get("date").asText();
        return new Branch(
            object.get("name").asText(),
            new CommitV1(
                commitSha,
                authorName,
                authorNickname,
                committerName,
                committerNickname,
                message,
                timestamp),
            object.get("protected").asBoolean());
      }
    }
  }

  private LoadingCache<String, BuildV1> builds =
      Caffeine.newBuilder().expireAfterWrite(Constants.CACHE_TIME).build(this::computeBuild);

  public LoadingCache<String, BuildV1> getBuilds() {
    return builds;
  }

  private BuildV1 computeBuild(String branch) throws IOException {
    Branch latestCommitBranch = branches.get(branch);
    if (latestCommitBranch == null) {
      return new BuildV1(-1, -1, new Branch("branch not found", null, false), null);
    }
    Call call =
        Constants.HTTP_CLIENT.newCall(
            new Request.Builder()
                .addHeader("User-Agent", "api.yatopia.net")
                .url(Constants.GITHUB_API_BUILDS)
                .build());
    try (Response response = call.execute()) {
      if (response.code() != 200) {
        return null;
      }
      try (InputStream in = response.body().byteStream()) {
        ObjectNode object = (ObjectNode) Constants.JSON_MAPPER.readTree(in);
        ArrayNode runs = (ArrayNode) object.get("workflow_runs");

        ObjectNode candidate = null;
        for (JsonNode node : runs) {
          if (node.get("head_branch").asText().equalsIgnoreCase(branch)) {
            if (node.get("head_sha")
                .asText()
                .equalsIgnoreCase(latestCommitBranch.getLatestCommit().getSha())) {
              if (node.get("conclusion").asText().equalsIgnoreCase("success")) {
                candidate = (ObjectNode) node;
                break;
              }
            } else {
              if (node.get("conclusion").asText().equalsIgnoreCase("success")) {
                if (candidate == null) {
                  candidate = (ObjectNode) node;
                } else {
                  int buildNumberNode = node.get("run_number").asInt();
                  int buildNumberCandidate = candidate.get("run_number").asInt();
                  if (buildNumberNode > buildNumberCandidate) {
                    candidate = (ObjectNode) node;
                  }
                }
                // we're not stopping until we find head_sha to be our latest commit sha
                // or if the build wasn't successful then get the latest successful one
              }
            }
          }
        }

        if (candidate == null) {
          return new BuildV1(-1, -1, new Branch("no successful build", null, false), null);
        }

        Call artifactsCall =
            Constants.HTTP_CLIENT.newCall(
                new Request.Builder()
                    .url(candidate.get("artifacts_url").asText())
                    .addHeader("User-Agent", "api.yatopia.net")
                    .build());
        try (Response artifactsResponse = artifactsCall.execute()) {
          if (artifactsResponse.code() != 200) {
            return null;
          }
          try (InputStream artifactsStream = artifactsResponse.body().byteStream()) {
            ObjectNode artifactsObject =
                (ObjectNode) Constants.JSON_MAPPER.readTree(artifactsStream);
            ArrayNode artifactsArray = (ArrayNode) artifactsObject.get("artifacts");
            List<Artifact> artifacts = new ArrayList<>();
            for (JsonNode artifactNode : artifactsArray) {
              ObjectNode artifact = (ObjectNode) artifactNode;

              String javaVersion = artifact.get("name").asText().split("-")[1];
              int id = artifact.get("id").asInt();
              String apiDownloadUrl = artifact.get("archive_download_url").asText();
              artifacts.add(new Artifact(id, apiDownloadUrl, javaVersion));
            }
            String suiteUrl = candidate.get("check_suite_url").asText();
            int suiteId = Integer.parseInt(suiteUrl.substring(suiteUrl.lastIndexOf('/') + 1));
            return new BuildV1(
                candidate.get("run_number").asInt(), suiteId, latestCommitBranch, artifacts);
          }
        }
      }
    }
  }
}
