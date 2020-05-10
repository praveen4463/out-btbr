package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.runner.provider.*;
import com.zylitics.btbr.util.CollectionUtil;
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
public class DaoBuildStatusProvider extends AbstractDaoProvider implements BuildStatusProvider {
  
  @Autowired
  DaoBuildStatusProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int saveOnStart(BuildStatusSaveOnStart buildStatusSaveOnStart) {
    Preconditions.checkNotNull(buildStatusSaveOnStart, "buildStatusSaveOnStart can't be null");
    
    String sql = "INSERT INTO bt_build_status (bt_build_id, bt_test_version_id" +
        ", status, start_date)" +
        " VALUES (:bt_build_id, :bt_test_version_id, :status, :start_date)";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(4));
    
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER,
        buildStatusSaveOnStart.getBuildId()));
  
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
        buildStatusSaveOnStart.getTestVersionId()));
  
    params.put("status", new SqlParameterValue(Types.OTHER,
        buildStatusSaveOnStart.getStatus()));
  
    params.put("start_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , buildStatusSaveOnStart.getStartDate()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public int saveWontStart(BuildStatusSaveWontStart buildStatusSaveWontStart) {
    Preconditions.checkNotNull(buildStatusSaveWontStart, "buildStatusSaveWontStart can't be null");
    
    String sql = "INSERT INTO bt_build_status (bt_build_id, bt_test_version_id, status)" +
        " VALUES (:bt_build_id, :bt_test_version_id, :status)";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(3));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER,
        buildStatusSaveWontStart.getBuildId()));
  
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
        buildStatusSaveWontStart.getTestVersionId()));
  
    params.put("status", new SqlParameterValue(Types.OTHER,
        buildStatusSaveWontStart.getStatus()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public int updateLine(BuildStatusUpdateLine buildStatusUpdateLine) {
    Preconditions.checkNotNull(buildStatusUpdateLine, "buildStatusUpdateLine can't be null");
    
    String sql = "UPDATE bt_build_status SET zwl_executing_line = :zwl_executing_line" +
        " WHERE bt_build_id = :bt_build_id and bt_test_version_id = :bt_test_version_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(3));
  
    params.put("zwl_executing_line", new SqlParameterValue(Types.INTEGER,
        buildStatusUpdateLine.getZwlExecutingLine()));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER,
        buildStatusUpdateLine.getBuildId()));
  
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
        buildStatusUpdateLine.getTestVersionId()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public int updateOnEnd(BuildStatusUpdateOnEnd buildStatusUpdateOnEnd) {
    Preconditions.checkNotNull(buildStatusUpdateOnEnd, "buildStatusUpdateOnEnd can't be null");
    
    String sql = "UPDATE bt_build_status SET status = :status" +
        ", end_date = :end_date, error = :error" +
        " WHERE bt_build_id = :bt_build_id and bt_test_version_id = :bt_test_version_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(5));

    params.put("status", new SqlParameterValue(Types.OTHER,
        buildStatusUpdateOnEnd.getStatus()));
  
    params.put("end_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , buildStatusUpdateOnEnd.getEndDate()));
  
    params.put("error", new SqlParameterValue(Types.OTHER, buildStatusUpdateOnEnd.getError()));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER,
        buildStatusUpdateOnEnd.getBuildId()));
  
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
        buildStatusUpdateOnEnd.getTestVersionId()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
}
