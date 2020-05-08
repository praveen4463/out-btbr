package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

public class BuildStatusSaveWontStart {
  
  private final int buildId;
  
  private final int testVersionId;
  
  private final TestStatus status;
  
  public BuildStatusSaveWontStart(int buildId, int testVersionId, TestStatus status) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkArgument(testVersionId > 0, "testVersionId is required");
    Preconditions.checkNotNull(status, "status can't be null");
    
    this.buildId = buildId;
    this.testVersionId = testVersionId;
    this.status = status;
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
  
  @Override
  public String toString() {
    return "BuildStatusSaveWontStart{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", status=" + status +
        '}';
  }
}