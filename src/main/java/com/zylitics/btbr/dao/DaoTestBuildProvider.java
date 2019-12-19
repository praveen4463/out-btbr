package com.zylitics.btbr.dao;

import com.zylitics.btbr.model.TestBuild;
import com.zylitics.btbr.runner.TestBuildProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Optional;

@Repository
public class DaoTestBuildProvider implements TestBuildProvider {
  
  private final NamedParameterJdbcTemplate jdbc;
  
  @Autowired
  DaoTestBuildProvider(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
  
  @Override
  public Optional<TestBuild> getTestBuild(int testBuildId) {
    String sql = "SELECT bt_test_version_id, bt_build_id FROM bt_test_build" +
        " WHERE bt_test_build_id = :bt_test_build_id;";
  
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_test_build_id"
        , new SqlParameterValue(Types.INTEGER, testBuildId));
    
    TestBuild testBuild = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        new TestBuild()
            .setTestBuildId(testBuildId)
            .setTestVersionId(rs.getInt("bt_test_version_id"))
            .setBuildId(rs.getInt("bt_build_id")));
    return Optional.ofNullable(testBuild);
  }
}
