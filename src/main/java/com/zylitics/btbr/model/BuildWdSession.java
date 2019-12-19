package com.zylitics.btbr.model;

import java.time.ZonedDateTime;

public class BuildWdSession {

  private int buildWdSessionId;
  
  private ZonedDateTime sessionEndDate;
  
  private boolean isSuccess;
  
  private String errorPostSession;
  
  public int getBuildWdSessionId() {
    return buildWdSessionId;
  }
  
  public BuildWdSession setBuildWdSessionId(int buildWdSessionId) {
    this.buildWdSessionId = buildWdSessionId;
    return this;
  }
  
  public ZonedDateTime getSessionEndDate() {
    return sessionEndDate;
  }
  
  public BuildWdSession setSessionEndDate(ZonedDateTime sessionEndDate) {
    this.sessionEndDate = sessionEndDate;
    return this;
  }
  
  public boolean isSuccess() {
    return isSuccess;
  }
  
  public BuildWdSession setSuccess(boolean success) {
    isSuccess = success;
    return this;
  }
  
  public String getErrorPostSession() {
    return errorPostSession;
  }
  
  public BuildWdSession setErrorPostSession(String errorPostSession) {
    this.errorPostSession = errorPostSession;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildWdSession{" +
        "buildWdSessionId=" + buildWdSessionId +
        ", sessionEndDate=" + sessionEndDate +
        ", isSuccess=" + isSuccess +
        ", errorPostSession='" + errorPostSession + '\'' +
        '}';
  }
}
