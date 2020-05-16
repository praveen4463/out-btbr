package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import java.util.Objects;

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
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BuildStatusSaveWontStart that = (BuildStatusSaveWontStart) o;
    return status == that.status;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), status);
  }
}