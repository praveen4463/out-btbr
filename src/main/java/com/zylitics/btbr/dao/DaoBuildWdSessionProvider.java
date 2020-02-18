package com.zylitics.btbr.dao;

import com.google.common.base.Strings;
import com.zylitics.btbr.model.BuildWdSession;
import com.zylitics.btbr.runner.provider.BuildWdSessionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

@Repository
class DaoBuildWdSessionProvider implements BuildWdSessionProvider {
  
  private final NamedParameterJdbcTemplate jdbc;
  
  @Autowired
  DaoBuildWdSessionProvider(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
  
  @Override
  public int updateBuildWdSession(BuildWdSession buildWdSession) {
    String sql = "UPDATE bt_build_wd_session SET session_end_date = :session_end_date" +
        ", is_success = :is_success, error_post_session = :error_post_session" +
        " WHERE bt_build_wd_session_id = :bt_build_wd_session_id;";
  
    Map<String, SqlParameterValue> params = new HashMap<>(6);
  
    params.put("bt_build_wd_session_id", new SqlParameterValue(Types.INTEGER
        , buildWdSession.getBuildWdSessionId()));
    params.put("session_end_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , buildWdSession.getSessionEndDate()));
    params.put("is_success", new SqlParameterValue(Types.BOOLEAN, buildWdSession.isSuccess()));
    params.put("error_post_session", new SqlParameterValue(
        Strings.isNullOrEmpty(buildWdSession.getErrorPostSession()) ? Types.NULL : Types.VARCHAR
        , buildWdSession.getErrorPostSession()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
}
