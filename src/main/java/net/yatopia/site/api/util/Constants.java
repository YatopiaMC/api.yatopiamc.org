package net.yatopia.site.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import okhttp3.OkHttpClient;

public class Constants {

  // settings for anybody forking
  public static final String GH_REPOSITORY = "YatopiaMC/Yatopia";
  public static final String DEFAULT_BRANCH = "ver/1.16.3";
  public static final int RATELIMIT = 100;
  public static final String API_BASE_URL = "https://api.yatopia.net/";

  public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

  public static final String BASE_GITHUB_API_URL =
      "https://api.github.com/repos/" + GH_REPOSITORY + "/";
  public static final String GITHUB_API_BRANCHES = BASE_GITHUB_API_URL + "branches/%s";
  public static final String GITHUB_API_BUILDS = BASE_GITHUB_API_URL + "actions/runs";

  public static final String JENKINS_JOB_BASE =
      "https://ci.codemc.io/job/YatopiaMC/job/Yatopia/job/%s/";
  public static final String JENKINS_JSON = JENKINS_JOB_BASE + "api/json";

  public static String getJenkinsBuildsUrlFor(String branch) {
    return String.format(JENKINS_JSON, URLEncoder.encode(branch));
  }

  public static String getJenkinsBuildDownloadUrlFor(String branch, int build) {
    return String.format(JENKINS_JOB_BASE, URLEncoder.encode(branch))
        + build
        + "/artifact/target/yatopia-"
        + branch.replace("ver/", "")
        + "-paperclip-b"
        + build
        + ".jar";
  }
}
