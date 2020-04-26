package com.zylitics.btbr.runner;

public class StopRequestException extends RuntimeException {
  
  private static final long serialVersionUID = 2870816434211739833L;
  
  public StopRequestException(String message) {
    super(message);
  }
}
