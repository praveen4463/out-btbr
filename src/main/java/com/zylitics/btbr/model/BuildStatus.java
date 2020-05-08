package com.zylitics.btbr.model;

import com.zylitics.btbr.runner.TestStatus;

import java.time.OffsetDateTime;

public class BuildStatus {
  
  private int buildId;
  
  private int testVersionId;
  
  private TestStatus status;
  
  private int zwlExecutingLine;
  
  private OffsetDateTime startDate;
  
  private OffsetDateTime endDate;
  
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
  
  public OffsetDateTime getStartDate() {
    return startDate;
  }
  
  public BuildStatus setStartDate(OffsetDateTime startDate) {
    this.startDate = startDate;
    return this;
  }
  
  public OffsetDateTime getEndDate() {
    return endDate;
  }
  
  public BuildStatus setEndDate(OffsetDateTime endDate) {
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
