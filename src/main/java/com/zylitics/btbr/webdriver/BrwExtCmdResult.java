package com.zylitics.btbr.webdriver;

public class BrwExtCmdResult {
  
  private long tookMillis;
  
  private boolean isSuccess;
  
  private boolean doNotAbortBuild;
  
  private String error;
  
  public long getTookMillis() {
    return tookMillis;
  }
  
  public BrwExtCmdResult setTookMillis(long tookMillis) {
    this.tookMillis = tookMillis;
    return this;
  }
  
  public boolean isSuccess() {
    return isSuccess;
  }
  
  public BrwExtCmdResult setSuccess(boolean success) {
    isSuccess = success;
    return this;
  }
  
  /**
   * Comes into affect only when isSuccess = false
   * Some commands even when failed, shouldn't lead to failure of entire build because their failure
   * is also expected, for example all brw-ext commands starting with 'verify'. If any such command
   * was encountered, build should move forward after marking the command 'failed'.
   * @return
   */
  public boolean isDoNotAbortBuild() {
    return doNotAbortBuild;
  }
  
  public BrwExtCmdResult setDoNotAbortBuild(boolean doNotAbortBuild) {
    this.doNotAbortBuild = doNotAbortBuild;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public BrwExtCmdResult setError(String error) {
    this.error = error;
    return this;
  }
  
  @Override
  public String toString() {
    return "BrwExtCmdResult{" +
        "tookMillis=" + tookMillis +
        ", isSuccess=" + isSuccess +
        ", doNotAbortBuild=" + doNotAbortBuild +
        ", error='" + error + '\'' +
        '}';
  }
}
