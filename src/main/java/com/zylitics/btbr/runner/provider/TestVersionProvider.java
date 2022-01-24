package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.TestVersion;

import java.util.List;
import java.util.Optional;

public interface TestVersionProvider {
  
  Optional<List<TestVersion>> getTestVersions(int buildId);
  
  Optional<TestVersion> getTestVersion(int projectId,
                                       String fileName,
                                       String testName,
                                       String versionName);
}
