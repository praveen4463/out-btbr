package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class BuildStatusUpdateOnEnd {
  
  private final int buildId;
  
  private final int testVersionId;
  
  private final TestStatus status;
  
  private final OffsetDateTime endDate;
  
  private final String error;
  
  public BuildStatusUpdateOnEnd(int buildId, int testVersionId, TestStatus status,
                                OffsetDateTime endDate, @Nullable String error) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkArgument(testVersionId > 0, "testVersionId is required");
    Preconditions.checkNotNull(status, "status can't be null");
    Preconditions.checkNotNull(endDate, "endDate can't be null");
    
    this.buildId = buildId;
    this.testVersionId = testVersionId;
    this.status = status;
    this.endDate = endDate;
    this.error = error;
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
  
  public OffsetDateTime getEndDate() {
    return endDate;
  }
  
  public String getError() {
    return error;
  }
  
  @Override
  public String toString() {
    return "BuildStatusUpdateOnEnd{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", status=" + status +
        ", endDate=" + endDate +
        ", error='" + error + '\'' +
        '}';
  }
}