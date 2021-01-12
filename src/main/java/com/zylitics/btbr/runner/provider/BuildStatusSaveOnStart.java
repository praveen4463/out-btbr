package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import java.time.OffsetDateTime;
import java.util.Objects;

public class BuildStatusSaveOnStart  extends AbstractBuildStatus {
  
  private final TestStatus status;
  
  private final OffsetDateTime startDate;
  
  private final int userId;
  
  public BuildStatusSaveOnStart(int buildId, int testVersionId, TestStatus status,
                                OffsetDateTime startDate, int userId) {
    super(buildId, testVersionId);
    Preconditions.checkNotNull(status, "status can't be null");
    Preconditions.checkNotNull(startDate, "startDate can't be null");
    Preconditions.checkArgument(userId > 0, "userId is required");
    
    this.status = status;
    this.startDate = startDate;
    this.userId = userId;
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  public OffsetDateTime getStartDate() {
    return startDate;
  }
  
  public int getUserId() {
    return userId;
  }
  
  @Override
  public String toString() {
    return "BuildStatusSaveOnStart{" +
        "status=" + status +
        ", startDate=" + startDate +
        ", userId=" + userId +
        "} " + super.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BuildStatusSaveOnStart that = (BuildStatusSaveOnStart) o;
    return userId == that.userId && status == that.status && startDate.equals(that.startDate);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), status, startDate, userId);
  }
}
