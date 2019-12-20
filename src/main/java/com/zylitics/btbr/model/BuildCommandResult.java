package com.zylitics.btbr.model;

public class BuildCommandResult {
  
  private int buildId;
  
  private int testVersionId;
  
  private long testCommandId;
  
  private int tookMillis;
  
  private boolean isSuccess;
  
  private String error;
  
  public int getBuildId() {
    return buildId;
  }
  
  public void setBuildId(int buildId) {
    this.buildId = buildId;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public void setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
  }
  
  public long getTestCommandId() {
    return testCommandId;
  }
  
  public void setTestCommandId(long testCommandId) {
    this.testCommandId = testCommandId;
  }
  
  public int getTookMillis() {
    return tookMillis;
  }
  
  public void setTookMillis(int tookMillis) {
    this.tookMillis = tookMillis;
  }
  
  public boolean isSuccess() {
    return isSuccess;
  }
  
  public void setSuccess(boolean success) {
    isSuccess = success;
  }
  
  public String getError() {
    return error;
  }
  
  public void setError(String error) {
    this.error = error;
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
