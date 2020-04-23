package com.zylitics.btbr.http;

public class ResponseBuildRun extends AbstractResponse {
  
  private String sessionId;
  
  public String getSessionId() {
    return sessionId;
  }
  
  public ResponseBuildRun setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }
}
