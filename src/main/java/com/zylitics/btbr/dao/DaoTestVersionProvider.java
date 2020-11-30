package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.File;
import com.zylitics.btbr.model.Test;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.TestVersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class DaoTestVersionProvider extends AbstractDaoProvider implements TestVersionProvider {
  
  @Autowired
  DaoTestVersionProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<List<TestVersion>> getTestVersions(int buildId) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    
    String sql = "SELECT" +
        " bt_test_version_id" +
        ", bt_test_version_name" +
        ", bt_test_version_code" +
        ", bt_test_id" +
        ", bt_test_name" +
        ", bt_file_id" +
        ", bt_file_name" +
        " FROM bt_build_tests" +
        " WHERE bt_build_id = :bt_build_id ORDER BY bt_build_tests_id;";
  
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    
    List<TestVersion> versions = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new TestVersion()
            .setTestVersionId(rs.getInt("bt_test_version_id"))
            .setName(rs.getString("bt_test_version_name"))
            .setCode(rs.getString("bt_test_version_code"))
            .setTest(new Test()
                .setTestId(rs.getInt("bt_test_id"))
                .setName(rs.getString("bt_test_name")))
            .setFile(new File()
                .setFileId(rs.getInt("bt_file_id"))
                .setName(rs.getString("bt_file_name"))));
    
    if (versions.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(versions);
  }
}
