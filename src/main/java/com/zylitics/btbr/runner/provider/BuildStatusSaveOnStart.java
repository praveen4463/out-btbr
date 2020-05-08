package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import java.time.OffsetDateTime;

public class BuildStatusSaveOnStart {
  
  private final int buildId;
  
  private final int testVersionId;
  
  private final TestStatus status;
  
  private final OffsetDateTime startDate;
  
  public BuildStatusSaveOnStart(int buildId, int testVersionId, TestStatus status,
                                OffsetDateTime startDate) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkArgument(testVersionId > 0, "testVersionId is required");
    Preconditions.checkNotNull(status, "status can't be null");
    Preconditions.checkNotNull(startDate, "startDate can't be null");
    
    this.buildId = buildId;
    this.testVersionId = testVersionId;
    this.status = status;
    this.startDate = startDate;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  public OffsetDateTime getStartDate() {
    return startDate;
  }
  
  @Override
  public String toString() {
    return "BuildStatusSaveOnStart{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", status=" + status +
        ", startDate=" + startDate +
        '}';
  }
}
