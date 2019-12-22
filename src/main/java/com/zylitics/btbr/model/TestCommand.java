package com.zylitics.btbr.model;

public class TestCommand {

  private long testCommandId;
  
  private int testVersionId;
  
  private String command;
  
  private String target;
  
  private String value;
  
  public long getTestCommandId() {
    return testCommandId;
  }
  
  public TestCommand setTestCommandId(long testCommandId) {
    this.testCommandId = testCommandId;
    return this;
  }
  
  public int getTestVersionId() {
    return testVersionId;
  }
  
  public TestCommand setTestVersionId(int testVersionId) {
    this.testVersionId = testVersionId;
    return this;
  }
  
  public String getCommand() {
    return command;
  }
  
  public TestCommand setCommand(String command) {
    this.command = command;
    return this;
  }
  
  public String getTarget() {
    return target;
  }
  
  public TestCommand setTarget(String target) {
    this.target = target;
    return this;
  }
  
  public String getValue() {
    return value;
  }
  
  public TestCommand setValue(String value) {
    this.value = value;
    return this;
  }
  
  @Override
  public String toString() {
    return "TestCommand{" +
        "testCommandId=" + testCommandId +
        ", testVersionId=" + testVersionId +
        ", command='" + command + '\'' +
        ", target='" + target + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}
