package com.zylitics.btbr.dao;

import com.google.common.base.Strings;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.runner.BuildProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class DaoBuildProvider implements BuildProvider {
  
  private final NamedParameterJdbcTemplate jdbc;
  
  @Autowired
  DaoBuildProvider(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
  
  @Override
  public Optional<Build> getBuild(int buildId) {
    String sql = "SELECT bt_build_id, build_key, bt_build_capability_id, bt_build_wd_session_id" +
        " FROM bt_build where bt_build_id = :bt_build_id;";
    
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    
    Build build = jdbc.queryForObject(sql, namedParams, (rs, rowNum) ->
        new Build()
            .setBuildId(buildId)
            .setBuildKey(rs.getString("build_key"))
            .setBuildCapabilityId(rs.getInt("bt_build_capability_id"))
            .setBuildWdSessionId(rs.getInt("bt_build_wd_session_id")));
    return Optional.ofNullable(build);
  }
  
  @Override
  public int updateBuild(Build build) {
    String sql = "UPDATE bt_build SET end_date = :end_date, is_success = :is_success" +
        ", error = :error WHERE bt_build_id = :bt_build_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(5);
    
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, build.getBuildId()));
    params.put("end_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , build.getEndDate()));
    params.put("is_success", new SqlParameterValue(Types.BOOLEAN, build.isSuccess()));
    params.put("error", new SqlParameterValue(
        Strings.isNullOrEmpty(build.getError()) ? Types.NULL : Types.VARCHAR, build.getError()));
        
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
    
    return jdbc.update(sql, namedParams);
  }
}
