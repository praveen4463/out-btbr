package com.zylitics.btbr.dao;

import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.TestVersionForGitRunProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoTestVersionForGitRunProvider extends AbstractTestVersionProvider
    implements TestVersionForGitRunProvider {
  
  @Autowired
  public DaoTestVersionForGitRunProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  
  @Override
  public Optional<List<TestVersion>> getTestVersionsWithoutCode(int buildId) {
    return super.getTestVersions(buildId, false);
  }
  
  @Override
  public Optional<TestVersion> getTestVersionWithoutCode(int projectId,
                                                         String fileName,
                                                         String testName,
                                                         String versionName) {
    return super.getTestVersion(projectId, fileName, testName, versionName, false);
  }
}
