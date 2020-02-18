package com.zylitics.btbr.model;

import java.time.ZonedDateTime;

public class ShotMetadata {
  
  private String shotName;
  
  private int buildId;
  
  private String buildKey;
  
  private String sessionKey;
  
  private long testCommandId;
  
  private ZonedDateTime createDate;
  
  public String getShotName() {
    return shotName;
  }
  
  public ShotMetadata setShotName(String shotName) {
    this.shotName = shotName;
    return this;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public ShotMetadata setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildKey() {
    return buildKey;
  }
  
  public ShotMetadata setBuildKey(String buildKey) {
    this.buildKey = buildKey;
    return this;
  }
  
  public String getSessionKey() {
    return sessionKey;
  }
  
  public ShotMetadata setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
    return this;
  }
  
  public long getTestCommandId() {
    return testCommandId;
  }
  
  public ShotMetadata setTestCommandId(long testCommandId) {
    this.testCommandId = testCommandId;
    return this;
  }
  
  public ZonedDateTime getCreateDate() {
    return createDate;
  }
  
  public ShotMetadata setCreateDate(ZonedDateTime createDate) {
    this.createDate = createDate;
    return this;
  }
  
  @Override
  public String toString() {
    return "ShotMetadata{" +
        "shotName='" + shotName + '\'' +
        ", buildId=" + buildId +
        ", buildKey='" + buildKey + '\'' +
        ", sessionKey='" + sessionKey + '\'' +
        ", testCommandId=" + testCommandId +
        ", createDate=" + createDate +
        '}';
  }
}
