package com.zylitics.btbr.runner.provider;

public interface BrowserProvider {
  
  String getDriverVersion(String browser, String version) throws RuntimeException;
}
