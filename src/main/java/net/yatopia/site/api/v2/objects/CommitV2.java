package net.yatopia.site.api.v2.objects;

public class CommitV2 {

  private final String sha, message, timestamp, comment;

  public CommitV2(String sha, String message, String timestamp, String comment) {
    this.sha = sha;
    this.message = message;
    this.timestamp = timestamp;
    this.comment = comment;
  }

  public String getSha() {
    return sha;
  }

  public String getMessage() {
    return message;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getComment() {
    return comment;
  }
}
