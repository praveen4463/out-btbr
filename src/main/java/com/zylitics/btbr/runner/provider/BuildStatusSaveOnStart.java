package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import java.time.OffsetDateTime;
import java.util.Objects;

public class BuildStatusSaveOnStart  extends AbstractBuildStatus {
  
  private final TestStatus status;
  
  private final OffsetDateTime startDate;
  
  public BuildStatusSaveOnStart(int buildId, int testVersionId, TestStatus status,
                                OffsetDateTime startDate) {
    super(buildId, testVersionId);
    Preconditions.checkNotNull(status, "status can't be null");
    Preconditions.checkNotNull(startDate, "startDate can't be null");
    
    this.status = status;
    this.startDate = startDate;
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
        "status=" + status +
        ", startDate=" + startDate +
        "} " + super.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BuildStatusSaveOnStart that = (BuildStatusSaveOnStart) o;
    return status == that.status &&
        Objects.equals(startDate, that.startDate);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), status, startDate);
  }
}
