package com.zylitics.btbr.shot;

public class ShotNameProvider {
  
  private final String sessionId;
  
  private final String buildKey;
  
  private final String extension;
  
  public ShotNameProvider(String sessionId, String buildKey, String extension) {
    this.sessionId = sessionId;
    this.buildKey = buildKey;
    this.extension = extension;
  }
  
  public String getName(String uniqueIdentifier) {
    return sessionId + "-" + buildKey + "-" + uniqueIdentifier + "." + extension;
  }
  
  public String getIdentifier(String shotName) {
    // sessionId may have '-' too thus its safe to get last index of it which will be just behind
    // identifier.
    return shotName.substring(shotName.lastIndexOf("-") + 1, shotName.lastIndexOf("."));
  }
}
