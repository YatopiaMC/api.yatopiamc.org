package net.yatopia.site.api.v1.objects;

public final class Commit {

  private final String sha, authorName, authorNickname, committerName, committerNickname, message, timestamp;

  public Commit(
      String sha,
      String authorName,
      String authorNickname,
      String committerName,
      String committerNickname,
      String message,
      String timestamp) {
    this.sha = sha;
    this.authorName = authorName;
    this.authorNickname = authorNickname;
    this.committerName = committerName;
    this.committerNickname = committerNickname;
    this.message = message;
    this.timestamp = timestamp;
  }

  public String getSha() {
    return sha;
  }

  public String getAuthorName() {
    return authorName;
  }

  public String getAuthorNickname() {
    return authorNickname;
  }

  public String getCommitterName() {
    return committerName;
  }

  public String getCommitterNickname() {
    return committerNickname;
  }

  public String getMessage() {
    return message;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
