package org.yatopiamc.site.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.yatopiamc.site.api.v2.CacheControlV2;
import org.yatopiamc.site.api.v2.objects.BuildV2;

public class StableBuildJSON {

  private LoadingCache<String, Integer> stableBuildCache =
      Caffeine.newBuilder().expireAfterWrite(Constants.CACHE_TIME).build(this::computeStableBuild);

  private final CacheControlV2 cacheControl;

  public StableBuildJSON(CacheControlV2 cacheControl) {
    this.cacheControl = cacheControl;
  }

  private int computeStableBuild(String branch) throws IOException {
    File stableBuildJSON = new File(".", "stableBuild.json");
    if (!stableBuildJSON.exists()) {
      BuildV2 build = cacheControl.getLatestSuccessfulBuild(branch);
      if (build.getNumber() != -1) {
        stableBuildJSON.createNewFile();
        try (Writer writer = new FileWriter(stableBuildJSON)) {
          ObjectNode node = Constants.JSON_MAPPER.createObjectNode();
          node.put(branch, build.getNumber());
          Constants.JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, node);
        }
        return build.getNumber();
      }
      return -1;
    }
    try (Reader reader = new FileReader(stableBuildJSON)) {
      JsonNode node = Constants.JSON_MAPPER.reader().readTree(reader);
      if (node.getNodeType() != JsonNodeType.OBJECT) {
        return -1;
      }
      JsonNode number = node.get(branch);
      if (number == null || number.isNull()) {
        return -1;
      }
      return number.asInt();
    }
  }

  public int getStableBuild(String branch) {
    Integer number = stableBuildCache.get(branch);
    if (number == null) {
      return -1;
    }
    return number;
  }

  public CacheControlV2 getCacheControl() {
    return cacheControl;
  }
}
