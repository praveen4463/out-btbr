package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.File;
import com.zylitics.btbr.model.Test;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.TestVersionProvider;
import com.zylitics.btbr.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DaoTestVersionProvider extends AbstractDaoProvider implements TestVersionProvider {
  
  @Autowired
  DaoTestVersionProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  private Optional<List<TestVersion>> getTestVersions(String sqlWhere,
                                                      SqlParameterSource namedParams) {
    
    String sql = "SELECT\n" +
        "bt_test_version_id\n" +
        ", bt_test_version_name\n" +
        ", bt_test_version_code\n" +
        ", bt_test_id\n" +
        ", bt_test_name\n" +
        ", bt_file_id\n" +
        ", bt_file_name\n" +
        "FROM bt_build_tests\n";
    sql += sqlWhere + "\n";
    sql += "ORDER BY bt_build_tests_id;";
    
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
  
  @Override
  public Optional<List<TestVersion>> getTestVersions(int buildId) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");
    
    String sqlWhere = "WHERE bt_build_id = :bt_build_id";
  
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    return getTestVersions(sqlWhere, namedParams);
  }
  
  @Override
  public Optional<TestVersion> getTestVersion(String fileName, String testName,
                                              String versionName) {
    String sqlWhere = "WHERE bt_file_name = :bt_file_name and bt_test_name = :bt_test_name\n" +
        "and bt_test_version_name = :bt_test_version_name";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(3));
    params.put("bt_file_name", new SqlParameterValue(Types.VARCHAR, fileName));
    params.put("bt_test_name", new SqlParameterValue(Types.VARCHAR, testName));
    params.put("bt_test_version_name", new SqlParameterValue(Types.VARCHAR, versionName));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
    
    return getTestVersions(sqlWhere, namedParams).map(r -> r.get(0));
  }
}
