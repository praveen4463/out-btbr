package com.zylitics.btbr.http;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.util.Objects;

@Validated
public class RequestBuildRun {
  
  @Min(1)
  private Integer buildId;
  
  private String vmDeleteUrl;
  
  public int getBuildId() {
    return buildId;
  }
  
  public void setBuildId(Integer buildId) {
    if (this.buildId == null) {
      this.buildId = buildId;
    }
  }
  
  public String getVmDeleteUrl() {
    return vmDeleteUrl;
  }
  
  public void setVmDeleteUrl(String vmDeleteUrl) {
    if (this.vmDeleteUrl == null) {
      this.vmDeleteUrl = vmDeleteUrl;
    }
  }
  
  @Override
  public String toString() {
    return "RequestBuildRun{" +
        "buildId=" + buildId +
        ", vmDeleteUrl='" + vmDeleteUrl + '\'' +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestBuildRun that = (RequestBuildRun) o;
    return buildId.equals(that.buildId) &&
        Objects.equals(vmDeleteUrl, that.vmDeleteUrl);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buildId, vmDeleteUrl);
  }
}
