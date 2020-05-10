package com.zylitics.btbr.model;

public class Build {

  private int buildId;
  
  private String buildKey;
  
  private BuildCapability buildCapability;
  
  private int buildVMId;
  
  private Boolean isSuccess;
  
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
  
  public Boolean isSuccess() {
    return isSuccess;
  }
  
  public Build setSuccess(Boolean success) {
    isSuccess = success;
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
        ", isSuccess=" + isSuccess +
        ", userId=" + userId +
        '}';
  }
}
