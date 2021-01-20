package org.yatopiamc.site.api.util;

public final class Pair<L, R> {

  public static <L, R> Pair<L, R> of(L left, R right) {
    return new Pair<>(left, right);
  }

  private final L left;
  private final R right;

  private Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L left() {
    return left;
  }

  public R right() {
    return right;
  }
}
