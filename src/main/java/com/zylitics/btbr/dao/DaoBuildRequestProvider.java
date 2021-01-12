package com.zylitics.btbr.dao;

import com.zylitics.btbr.runner.provider.BuildRequestProvider;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
public class DaoBuildRequestProvider extends AbstractDaoProvider implements BuildRequestProvider {
  
  DaoBuildRequestProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int markBuildRequestCompleted(long buildRequestId) {
    String sql = "UPDATE bt_build_request SET completed = true\n" +
        " WHERE bt_build_request_id = :bt_build_request_id";
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_request_id",
        new SqlParameterValue(Types.BIGINT, buildRequestId));
    return jdbc.update(sql, namedParams);
  }
}
