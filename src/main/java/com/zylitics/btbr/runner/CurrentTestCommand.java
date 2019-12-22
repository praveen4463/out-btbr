package com.zylitics.btbr.runner;

public class CurrentTestCommand {
  
  private long testCommandId;
  
  private String webDriverCommand;
  
  private String wdcHttpMethod;
  
  public long getTestCommandId() {
    return testCommandId;
  }
  
  public CurrentTestCommand setTestCommandId(long testCommandId) {
    this.testCommandId = testCommandId;
    return this;
  }
  
  public String getWebDriverCommand() {
    return webDriverCommand;
  }
  
  public CurrentTestCommand setWebDriverCommand(String webDriverCommand) {
    this.webDriverCommand = webDriverCommand;
    return this;
  }
  
  public String getWdcHttpMethod() {
    return wdcHttpMethod;
  }
  
  public CurrentTestCommand setWdcHttpMethod(String wdcHttpMethod) {
    this.wdcHttpMethod = wdcHttpMethod;
    return this;
  }
  
  @Override
  public String toString() {
    return "CurrentTestCommand{" +
        "testCommandId=" + testCommandId +
        ", webDriverCommand='" + webDriverCommand + '\'' +
        ", wdcHttpMethod='" + wdcHttpMethod + '\'' +
        '}';
  }
}
