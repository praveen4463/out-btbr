package com.zylitics.btbr.runner.provider;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.TestStatus;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class BuildStatusUpdateOnEnd extends AbstractBuildStatus {
  
  private final TestStatus status;
  
  private final OffsetDateTime endDate;
  
  private final String error;
  
  public BuildStatusUpdateOnEnd(int buildId, int testVersionId, TestStatus status,
                                OffsetDateTime endDate, @Nullable String error) {
    super(buildId, testVersionId);
    Preconditions.checkNotNull(status, "status can't be null");
    Preconditions.checkNotNull(endDate, "endDate can't be null");
    
    this.status = status;
    this.endDate = endDate;
    this.error = error;
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
        "status=" + status +
        ", endDate=" + endDate +
        ", error='" + error + '\'' +
        "} " + super.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BuildStatusUpdateOnEnd that = (BuildStatusUpdateOnEnd) o;
    return status == that.status &&
        endDate.equals(that.endDate) &&
        Objects.equals(error, that.error);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), status, endDate, error);
  }
}