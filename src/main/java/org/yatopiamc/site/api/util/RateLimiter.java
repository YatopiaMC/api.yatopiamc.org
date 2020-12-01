package org.yatopiamc.site.api.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {

  private static Cache<String, AtomicInteger> leftRequests =
      Caffeine.newBuilder().expireAfterWrite(Constants.RATELIMIT_PER).build();

  public static boolean canProceed(String address) {
    AtomicInteger requests = leftRequests.getIfPresent(address);
    if (requests == null) {
      leftRequests.put(address, new AtomicInteger(Constants.RATELIMIT - 1));
    } else {
      int left = requests.decrementAndGet();
      if (left <= 0) {
        return false;
      }
      leftRequests.invalidate(address);
      leftRequests.put(address, requests);
    }
    return true;
  }
}
