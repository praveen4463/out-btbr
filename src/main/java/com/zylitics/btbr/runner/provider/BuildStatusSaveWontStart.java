package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

public class BuildStatusSaveWontStart extends AbstractBuildStatus {
  
  private final TestStatus status;
  
  public BuildStatusSaveWontStart(int buildId, int testVersionId, TestStatus status) {
    super(buildId, testVersionId);
    Preconditions.checkNotNull(status, "status can't be null");

    this.status = status;
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  @Override
  public String toString() {
    return "BuildStatusSaveWontStart{" +
        "status=" + status +
        "} " + super.toString();
  }
}