package net.yatopia.site.api.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {

  public enum RouteType {
    DOWNLOAD,
    NORMAL
  }

  private static Cache<String, AtomicInteger> leftRequests =
      Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();
  private static Map<String, RouteType> previousRouteType = new ConcurrentHashMap<>();

  public static boolean canProceed(String address) {
    return canProceed(address, RouteType.NORMAL, Constants.DEFAULT_RATELIMIT);
  }

  public static boolean canProceed(String address, RouteType routeType, int limit) {
    AtomicInteger requests = leftRequests.getIfPresent(address);
    if (requests == null) {
      leftRequests.put(address, new AtomicInteger(limit - 1));
      previousRouteType.put(address, routeType);
    } else {
      RouteType previous = previousRouteType.get(address);
      if (previous == routeType) {
        int left = requests.decrementAndGet();
        if (left <= 0) {
          return false;
        }
        leftRequests.invalidate(address);
        leftRequests.put(address, requests);
      }
      if (previous == RouteType.NORMAL && routeType == RouteType.DOWNLOAD) {
        leftRequests.invalidate(address);
        leftRequests.put(address, new AtomicInteger(limit - 1));
        previousRouteType.replace(address, routeType);
      }
      if (previous == RouteType.DOWNLOAD && routeType == RouteType.NORMAL) {
        int left = leftRequests.getIfPresent(address).get();
        leftRequests.invalidate(address);
        leftRequests.put(address, new AtomicInteger(limit - (Constants.DOWNLOAD_RATELIMIT - left)));
        previousRouteType.replace(address, routeType);
      }
    }
    return true;
  }
}
