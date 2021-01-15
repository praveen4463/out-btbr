package com.zylitics.btbr.model;

import java.time.OffsetDateTime;

public class BuildOutput {
  
  private int buildId;
  
  private int testVersionId;
  
  private String output;
  
  private boolean ended;
  
  private OffsetDateTime createDate;
  
  public int getBuildId() {
    return buildId;
  }
  
  public BuildOutput setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public BuildOutput setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public String getOutput() {
    return output;
  }
  
  public BuildOutput setOutput(String output) {
    this.output = output;
    return this;
  }
  
  public boolean isEnded() {
    return ended;
  }
  
  public BuildOutput setEnded(boolean ended) {
    this.ended = ended;
    return this;
  }
  
  public OffsetDateTime getCreateDate() {
    return createDate;
  }
  
  public BuildOutput setCreateDate(OffsetDateTime createDate) {
    this.createDate = createDate;
    return this;
  }
}
