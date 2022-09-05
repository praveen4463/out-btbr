package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.TestVersion;

import java.util.List;
import java.util.Optional;

public interface TestVersionForGitRunProvider {
  
  Optional<List<TestVersion>> getTestVersionsWithoutCode(int buildId);
  
  Optional<TestVersion> getTestVersionWithoutCode(int projectId,
                                       String fileName,
                                       String testName,
                                       String versionName);
}
