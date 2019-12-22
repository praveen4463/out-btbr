package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.TestBuild;

import java.util.Optional;

public interface TestBuildProvider {
  
  Optional<TestBuild> getTestBuild(int testBuildId);
}
