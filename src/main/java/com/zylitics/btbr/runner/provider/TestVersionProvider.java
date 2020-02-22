package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.TestVersion;

import java.util.List;
import java.util.Optional;

public interface TestVersionProvider {
  
  Optional<List<TestVersion>> getTestVersion(int buildId);
}
