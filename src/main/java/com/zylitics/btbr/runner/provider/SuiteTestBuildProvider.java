package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.SuiteTestBuild;

import java.util.Optional;

public interface SuiteTestBuildProvider {
  
  Optional<SuiteTestBuild> getSuiteTestBuild(int suiteTestBuildId);
}
