package com.zylitics.btbr.http;

public class BaseResponse {
  
  private String status;
  
  private String error;
  
  private int httpStatusCode;
  
  public String getStatus() {
    return status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public String getError() {
    return error;
  }
  
  public void setError(String error) {
    this.error = error;
  }
  
  public int getHttpStatusCode() {
    return httpStatusCode;
  }
  
  public void setHttpStatusCode(int httpErrorStatusCode) {
    this.httpStatusCode = httpErrorStatusCode;
  }
}