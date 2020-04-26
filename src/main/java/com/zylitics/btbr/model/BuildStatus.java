package com.zylitics.btbr.model;

import com.zylitics.btbr.runner.TestStatus;

import java.time.ZonedDateTime;

public class BuildStatus {
  
  private int buildId;
  
  private int testVersionId;
  
  private TestStatus status;
  
  private int zwlExecutingLine;
  
  private ZonedDateTime startDate;
  
  private ZonedDateTime endDate;
  
  private String error;
  
  public int getBuildId() {
    return buildId;
  }
  
  public BuildStatus setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public BuildStatus setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  public BuildStatus setStatus(TestStatus status) {
    this.status = status;
    return this;
  }
  
  public int getZwlExecutingLine() {
    return zwlExecutingLine;
  }
  
  public BuildStatus setZwlExecutingLine(int zwlExecutingLine) {
    this.zwlExecutingLine = zwlExecutingLine;
    return this;
  }
  
  public ZonedDateTime getStartDate() {
    return startDate;
  }
  
  public BuildStatus setStartDate(ZonedDateTime startDate) {
    this.startDate = startDate;
    return this;
  }
  
  public ZonedDateTime getEndDate() {
    return endDate;
  }
  
  public BuildStatus setEndDate(ZonedDateTime endDate) {
    this.endDate = endDate;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public BuildStatus setError(String error) {
    this.error = error;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildStatus{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", status='" + status + '\'' +
        ", zwlExecutingLine=" + zwlExecutingLine +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", error='" + error + '\'' +
        '}';
  }
}
