package com.zylitics.btbr.model;

public class ZwlProgram {
  
  private String code;
  
  public String getCode() {
    return code;
  }
  
  public ZwlProgram setCode(String code) {
    this.code = code;
    return this;
  }
  
  @Override
  public String toString() {
    return "ZwlProgram{" +
        "code='" + code + '\'' +
        '}';
  }
}
