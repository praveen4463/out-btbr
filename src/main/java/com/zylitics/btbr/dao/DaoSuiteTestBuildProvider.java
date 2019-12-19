package com.zylitics.btbr.dao;

import com.google.common.base.Splitter;
import com.zylitics.btbr.model.SuiteTestBuild;
import com.zylitics.btbr.runner.SuiteTestBuildProvider;
import jdk.jfr.internal.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DaoSuiteTestBuildProvider implements SuiteTestBuildProvider {
  
  private final NamedParameterJdbcTemplate jdbc;
  
  @Autowired
  DaoSuiteTestBuildProvider(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
  
  @Override
  public Optional<SuiteTestBuild> getSuiteTestBuild(int suiteTestBuildId) {
    String sql = "SELECT stb.bt_build_id" +
        ", string_agg(stc.bt_test_version_id, ',' ORDER BY stc.bt_suite_test_content_id)" +
        " AS test_versions FROM bt_suite_test_build AS stb INNER JOIN bt_suite_test_content" +
        " AS stc ON (stb.bt_suite_test_version_id = stc.bt_suite_test_version_id) WHERE" +
        " stb.bt_suite_test_build_id = :bt_suite_test_build_id GROUP BY stb.bt_build_id;";
  
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_suite_test_build_id"
        , new SqlParameterValue(Types.INTEGER, suiteTestBuildId));
  
    SuiteTestBuild suiteTestBuild = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        new SuiteTestBuild()
            .setSuiteTestBuildId(suiteTestBuildId)
            .setTestVersionIds(
                Splitter.on(",").splitToList(rs.getString("test_versions"))
                    .stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList()))
            .setBuildId(rs.getInt("bt_build_id")));
    return Optional.ofNullable(suiteTestBuild);
  }
}
