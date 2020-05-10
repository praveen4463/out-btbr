package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;

abstract class AbstractBuildStatus {
  
  private final int buildId;
  
  private final int testVersionId;
  
  AbstractBuildStatus(int buildId, int testVersionId) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    Preconditions.checkArgument(testVersionId > 0, "testVersionId is required");
  
    this.buildId = buildId;
    this.testVersionId = testVersionId;
  }
  
  public int getBuildId() {
    return buildId;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  @Override
  public String toString() {
    return "AbstractBuildStatus{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        '}';
  }
}
