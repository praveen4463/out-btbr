package com.zylitics.btbr.model;

import java.time.OffsetDateTime;

public class ShotMetadata {
  
  private String shotName;
  
  private int buildId;
  
  private int testVersionId;
  
  private String buildKey;
  
  private String sessionKey;
  
  private int atLineZwl;
  
  private OffsetDateTime createDate;
  
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
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public ShotMetadata setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
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
  
  public int getAtLineZwl() {
    return atLineZwl;
  }
  
  public ShotMetadata setAtLineZwl(int atLineZwl) {
    this.atLineZwl = atLineZwl;
    return this;
  }
  
  public OffsetDateTime getCreateDate() {
    return createDate;
  }
  
  public ShotMetadata setCreateDate(OffsetDateTime createDate) {
    this.createDate = createDate;
    return this;
  }
  
  @Override
  public String toString() {
    return "ShotMetadata{" +
        "shotName='" + shotName + '\'' +
        ", buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", buildKey='" + buildKey + '\'' +
        ", sessionKey='" + sessionKey + '\'' +
        ", atLineZwl=" + atLineZwl +
        ", createDate=" + createDate +
        '}';
  }
}
