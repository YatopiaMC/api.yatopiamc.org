package net.yatopia.site.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

public class Constants {

  // settings for anybody forking
  public static final String GH_REPOSITORY = "YatopiaMC/Yatopia";
  public static final String DEFAULT_BRANCH = "ver/1.16.1";
  public static final int DEFAULT_RATELIMIT = 150;
  public static final int DOWNLOAD_RATELIMIT = 10;

  public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

  public static final String BASE_GITHUB_API_URL =
      "https://api.github.com/repos/" + GH_REPOSITORY + "/";
  public static final String GITHUB_API_BRANCHES = BASE_GITHUB_API_URL + "branches/%s";
  public static final String GITHUB_API_BUILDS = BASE_GITHUB_API_URL + "actions/runs";
}
