package com.zylitics.btbr.http;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.util.Objects;

@Validated
public class RequestBuildRun {
  
  @Min(1)
  private Integer buildId;
  
  public int getBuildId() {
    return buildId;
  }
  
  public void setBuildId(Integer buildId) {
    if (this.buildId == null) {
      this.buildId = buildId;
    }
  }
  
  @Override
  public String toString() {
    return "RequestBuildRun{" +
        "buildId=" + buildId +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestBuildRun that = (RequestBuildRun) o;
    return buildId.equals(that.buildId);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buildId);
  }
}
