package com.zylitics.btbr.dao;

import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.TestVersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoTestVersionProvider extends AbstractTestVersionProvider
    implements TestVersionProvider {
  
  @Autowired
  public DaoTestVersionProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<List<TestVersion>> getTestVersions(int buildId) {
    return super.getTestVersions(buildId);
  }

  @Override
  public Optional<TestVersion> getFunctionAsTestVersion(int projectId,
                                                        String fileName,
                                                        String testName,
                                                        String versionName) {
    return super.getTestVersion(projectId, fileName, testName, versionName);
  }
}
