package com.zylitics.btbr.test.model;

import com.zylitics.btbr.runner.TestStatus;

import java.time.LocalDateTime;

public class BuildStatus {
  
  private TestStatus status;
  
  private int zwlExecutingLine;
  
  private LocalDateTime startDate;
  
  private LocalDateTime endDate;
  
  private String error;
  
  private String errorFrom;
  
  private String errorTo;
  
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
  
  public LocalDateTime getStartDate() {
    return startDate;
  }
  
  public BuildStatus setStartDate(LocalDateTime startDate) {
    this.startDate = startDate;
    return this;
  }
  
  public LocalDateTime getEndDate() {
    return endDate;
  }
  
  public BuildStatus setEndDate(LocalDateTime endDate) {
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
  
  public String getErrorFrom() {
    return errorFrom;
  }
  
  public BuildStatus setErrorFrom(String errorFrom) {
    this.errorFrom = errorFrom;
    return this;
  }
  
  public String getErrorTo() {
    return errorTo;
  }
  
  public BuildStatus setErrorTo(String errorTo) {
    this.errorTo = errorTo;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildStatus{" +
        "status=" + status +
        ", zwlExecutingLine=" + zwlExecutingLine +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", error='" + error + '\'' +
        ", errorFrom='" + errorFrom + '\'' +
        ", errorTo='" + errorTo + '\'' +
        '}';
  }
}
