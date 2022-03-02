package com.zylitics.btbr.model;

public class FailedTestDetail {
  
  private TestVersion testVersion;
  
  private String error;
  
  private String url;
  
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
}
