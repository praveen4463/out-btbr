package com.zylitics.btbr.runner.provider;

import java.util.Optional;

public interface BrowserProvider {
  
  Optional<String> getDriverVersion(String browser, String version) throws RuntimeException;
}
