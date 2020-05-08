package com.zylitics.btbr.model;

import java.time.OffsetDateTime;

public class ZwlProgramOutput {
  
  private int buildId;
  
  private int testVersionId;
  
  private String output;
  
  private boolean ended;
  
  private OffsetDateTime createDate;
  
  public int getBuildId() {
    return buildId;
  }
  
  public ZwlProgramOutput setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public ZwlProgramOutput setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public String getOutput() {
    return output;
  }
  
  public ZwlProgramOutput setOutput(String output) {
    this.output = output;
    return this;
  }
  
  public boolean isEnded() {
    return ended;
  }
  
  public ZwlProgramOutput setEnded(boolean ended) {
    this.ended = ended;
    return this;
  }
  
  public OffsetDateTime getCreateDate() {
    return createDate;
  }
  
  public ZwlProgramOutput setCreateDate(OffsetDateTime createDate) {
    this.createDate = createDate;
    return this;
  }
  
  @Override
  public String toString() {
    return "ZwlProgramOutput{" +
        "buildId=" + buildId +
        ", testVersionId=" + testVersionId +
        ", output='" + output + '\'' +
        ", ended=" + ended +
        ", createDate=" + createDate +
        '}';
  }
}
