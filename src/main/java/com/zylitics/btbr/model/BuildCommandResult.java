package com.zylitics.btbr.model;

public class BuildCommandResult {
  
  private int buildId;
  
  private int testVersionId;
  
  private long testCommandId;
  
  private long tookMillis;
  
  private boolean isSuccess;
  
  private String error;
  
  public int getBuildId() {
    return buildId;
  }
  
  public BuildCommandResult setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public BuildCommandResult setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public long getTestCommandId() {
    return testCommandId;
  }
  
  public BuildCommandResult setTestCommandId(long testCommandId) {
    this.testCommandId = testCommandId;
    return this;
  }
  
  public long getTookMillis() {
    return tookMillis;
  }
  
  public BuildCommandResult setTookMillis(long tookMillis) {
    this.tookMillis = tookMillis;
    return this;
  }
  
  public boolean isSuccess() {
    return isSuccess;
  }
  
  public BuildCommandResult setSuccess(boolean success) {
    isSuccess = success;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public BuildCommandResult setError(String error) {
    this.error = error;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildCommandResult{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", testCommandId=" + testCommandId +
        ", tookMillis=" + tookMillis +
        ", isSuccess=" + isSuccess +
        ", error='" + error + '\'' +
        '}';
  }
}
