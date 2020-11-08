package net.yatopia.site.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.Duration;
import okhttp3.OkHttpClient;

public class Constants {

  // settings for anybody forking
  public static final String GH_REPOSITORY = "YatopiaMC/Yatopia";
  public static final String DEFAULT_BRANCH = "ver/1.16.4";
  public static final int RATELIMIT = 20;
  public static final Duration RATELIMIT_PER = Duration.ofMinutes(2);
  public static final Duration CACHE_TIME = Duration.ofMinutes(15);
  public static final String API_BASE_URL = "https://api.yatopia.net/";
  public static final String JENKINS_JOB_BASE =
      "https://ci.codemc.io/job/YatopiaMC/job/Yatopia/job/%s/";
  public static final int BUILDS_LISTED = 15;
  public static final String CHANGESETS_WORD =
      "changeSets"; // differs on different jenkins installations

  public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

  public static final String BASE_GITHUB_API_URL =
      "https://api.github.com/repos/" + GH_REPOSITORY + "/";
  public static final String GITHUB_API_BRANCHES = BASE_GITHUB_API_URL + "branches/%s";
  public static final String GITHUB_API_BUILDS = BASE_GITHUB_API_URL + "actions/runs";

  public static final String JENKINS_LAST_BUILDS =
      JENKINS_JOB_BASE
          + "/api/json?tree=builds[number,url,result,"
          + CHANGESETS_WORD
          + "[items[comment,commitId,msg,date]]]{,"
          + BUILDS_LISTED
          + "}";

  public static String getJenkinsLastBuildsUrlFor(String branch) throws IOException {
    return String.format(JENKINS_LAST_BUILDS, URLEncoder.encode(branch, "UTF-8"));
  }

  // todo: non-permanent version of this
  public static String getJenkinsBuildDownloadUrlFor(String branch, int build) throws IOException {
    return String.format(JENKINS_JOB_BASE, URLEncoder.encode(branch, "UTF-8"))
        + build
        + "/artifact/target/yatopia-"
        + branch.replace("ver/", "")
        + "-paperclip-b"
        + build
        + ".jar";
  }
}
