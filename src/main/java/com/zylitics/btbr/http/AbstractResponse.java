package com.zylitics.btbr.http;

public abstract class AbstractResponse {
  
  private String status;
  
  private String error;
  
  private int httpStatusCode;
  
  public String getStatus() {
    return status;
  }
  
  public AbstractResponse setStatus(String status) {
    this.status = status;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public AbstractResponse setError(String error) {
    this.error = error;
    return this;
  }
  
  public int getHttpStatusCode() {
    return httpStatusCode;
  }
  
  public AbstractResponse setHttpStatusCode(int httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
    return this;
  }
}