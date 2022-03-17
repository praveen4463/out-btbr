package com.zylitics.btbr.model;

import java.time.OffsetDateTime;

public class FailedTestDetail {
  
  private TestVersion testVersion;
  
  private String error;
  
  private String url;
  
  private OffsetDateTime timestamp;
  
  public TestVersion getTestVersion() {
    return testVersion;
  }
  
  public FailedTestDetail setTestVersion(TestVersion testVersion) {
    this.testVersion = testVersion;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public FailedTestDetail setError(String error) {
    this.error = error;
    return this;
  }
  
  public String getUrl() {
    return url;
  }
  
  public FailedTestDetail setUrl(String url) {
    this.url = url;
    return this;
  }
  
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }
  
  public FailedTestDetail setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }
}
