package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import java.util.Objects;

public class BuildStatusSaveWontStart extends AbstractBuildStatus {
  
  private final TestStatus status;
  
  private final int userId;
  
  public BuildStatusSaveWontStart(int buildId, int testVersionId, TestStatus status, int userId) {
    super(buildId, testVersionId);
    Preconditions.checkNotNull(status, "status can't be null");
    Preconditions.checkArgument(userId > 0, "userId is required");

    this.status = status;
    this.userId = userId;
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  public int getUserId() {
    return userId;
  }
  
  @Override
  public String toString() {
    return "BuildStatusSaveWontStart{" +
        "status=" + status +
        ", userId=" + userId +
        "} " + super.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BuildStatusSaveWontStart that = (BuildStatusSaveWontStart) o;
    return userId == that.userId && status == that.status;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), status, userId);
  }
}