package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.model.BuildStatus;
import com.zylitics.btbr.runner.provider.BuildStatusProvider;
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
  public int save(BuildStatus buildStatus) {
    Preconditions.checkNotNull(buildStatus, "buildStatus can't be null");
    Preconditions.checkArgument(buildStatus.getBuildId() > 0, "buildId is required");
    Preconditions.checkArgument(buildStatus.getTestVersionId() > 0, "testVersionId is required");
    Preconditions.checkNotNull(buildStatus.getStartDate(), "startDate can't be null");
    Preconditions.checkNotNull(buildStatus.getStatus(), "status can't be null");
    
    String sql = "INSERT INTO bt_build_status (bt_build_id, bt_test_version_id" +
        ", status, start_date)" +
        " VALUES (:bt_build_id, :bt_test_version_id, :status, :start_date)";
    
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(4));
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildStatus.getBuildId()));
    
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
        buildStatus.getTestVersionId()));
  
    params.put("status", new SqlParameterValue(Types.VARCHAR, buildStatus.getStatus().name()));
    
    params.put("start_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , buildStatus.getStartDate()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public int update(BuildStatus buildStatus) {
    Preconditions.checkNotNull(buildStatus, "buildStatus can't be null");
    Preconditions.checkArgument(buildStatus.getBuildId() > 0, "buildId is required");
    Preconditions.checkArgument(buildStatus.getTestVersionId() > 0, "testVersionId is required");
    Preconditions.checkNotNull(buildStatus.getEndDate(), "endDate can't be null");
    Preconditions.checkNotNull(buildStatus.getStatus(), "status can't be null");
    
    
    String sql = "UPDATE bt_build_status SET status = :status" +
        ", zwl_executing_line = :zwl_executing_line" +
        ", end_date = :end_date, error = :error" +
        " WHERE bt_build_id = :bt_build_id and bt_test_version_id = :bt_test_version_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(5));
    
    params.put("status", new SqlParameterValue(Types.VARCHAR, buildStatus.getStatus().name()));
  
    params.put("end_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE
        , buildStatus.getEndDate()));
  
    params.put("error", new SqlParameterValue(Strings.isNullOrEmpty(buildStatus.getError())
        ? Types.NULL : Types.VARCHAR, buildStatus.getError()));
    
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildStatus.getBuildId()));
  
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
        buildStatus.getTestVersionId()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public int updateLine(BuildStatus buildStatus) {
    Preconditions.checkNotNull(buildStatus, "buildStatus can't be null");
    Preconditions.checkArgument(buildStatus.getBuildId() > 0, "buildId is required");
    Preconditions.checkArgument(buildStatus.getTestVersionId() > 0, "testVersionId is required");
    Preconditions.checkArgument(buildStatus.getZwlExecutingLine() > 0,
        "zwlExecutingLine is required");
    
    String sql = "UPDATE bt_build_status SET zwl_executing_line = :zwl_executing_line" +
        " WHERE bt_build_id = :bt_build_id and bt_test_version_id = :bt_test_version_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(3));
  
    params.put("zwl_executing_line",
        new SqlParameterValue(Types.INTEGER, buildStatus.getZwlExecutingLine()));
  
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildStatus.getBuildId()));
  
    params.put("bt_test_version_id", new SqlParameterValue(Types.INTEGER,
        buildStatus.getTestVersionId()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
}
