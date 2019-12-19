package com.zylitics.btbr.model;

import java.time.ZonedDateTime;

public class Build {

  private int buildId;
  
  private String key;
  
  private int buildCapabilityId;
  
  private int buildWdSessionId;
  
  private ZonedDateTime endDate;
  
  private boolean isSuccess;
  
  private String error;
  
  public int getBuildId() {
    return buildId;
  }
  
  public Build setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getKey() {
    return key;
  }
  
  public Build setKey(String key) {
    this.key = key;
    return this;
  }
  
  public int getBuildCapabilityId() {
    return buildCapabilityId;
  }
  
  public Build setBuildCapabilityId(int buildCapabilityId) {
    this.buildCapabilityId = buildCapabilityId;
    return this;
  }
  
  public int getBuildWdSessionId() {
    return buildWdSessionId;
  }
  
  public Build setBuildWdSessionId(int buildWdSessionId) {
    this.buildWdSessionId = buildWdSessionId;
    return this;
  }
  
  public ZonedDateTime getEndDate() {
    return endDate;
  }
  
  public Build setEndDate(ZonedDateTime endDate) {
    this.endDate = endDate;
    return this;
  }
  
  public boolean isSuccess() {
    return isSuccess;
  }
  
  public Build setSuccess(boolean success) {
    isSuccess = success;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public Build setError(String error) {
    this.error = error;
    return this;
  }
  
  @Override
  public String toString() {
    return "Build{" +
        "buildId=" + buildId +
        ", key='" + key + '\'' +
        ", buildCapabilityId=" + buildCapabilityId +
        ", buildWdSessionId=" + buildWdSessionId +
        ", endDate=" + endDate +
        ", isSuccess=" + isSuccess +
        ", error='" + error + '\'' +
        '}';
  }
}
