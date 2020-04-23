package com.zylitics.btbr.model;

import java.time.ZonedDateTime;

public class Build {

  private int buildId;
  
  private String buildKey;
  
  private BuildCapability buildCapability;
  
  private int buildVMId;
  
  private ZonedDateTime endDate;
  
  private boolean isSuccess;
  
  private String error;
  
  private int userId;
  
  public int getBuildId() {
    return buildId;
  }
  
  public Build setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildKey() {
    return buildKey;
  }
  
  public Build setBuildKey(String buildKey) {
    this.buildKey = buildKey;
    return this;
  }
  
  public BuildCapability getBuildCapability() {
    return buildCapability;
  }
  
  public Build setBuildCapability(BuildCapability buildCapability) {
    this.buildCapability = buildCapability;
    return this;
  }
  
  public int getBuildVMId() {
    return buildVMId;
  }
  
  public Build setBuildVMId(int buildVMId) {
    this.buildVMId = buildVMId;
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
  
  public int getUserId() {
    return userId;
  }
  
  public Build setUserId(int userId) {
    this.userId = userId;
    return this;
  }
  
  @Override
  public String toString() {
    return "Build{" +
        "buildId=" + buildId +
        ", buildKey='" + buildKey + '\'' +
        ", buildCapability=" + buildCapability +
        ", buildVMId=" + buildVMId +
        ", endDate=" + endDate +
        ", isSuccess=" + isSuccess +
        ", error='" + error + '\'' +
        ", userId=" + userId +
        '}';
  }
}
