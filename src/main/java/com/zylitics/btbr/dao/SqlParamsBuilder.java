package com.zylitics.btbr.dao;

import com.zylitics.btbr.util.DateTimeUtil;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.JDBCType;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class SqlParamsBuilder {
  
  private final Map<String, SqlParameterValue> params = new HashMap<>();
  
  public SqlParamsBuilder(int userId) {
    withInteger("zluser_id", userId);
  }
  
  public SqlParamsBuilder(int projectId, int userId) {
    withInteger("bt_project_id", projectId);
    withInteger("zluser_id", userId);
  }
  
  public SqlParamsBuilder() {}
  
  public SqlParamsBuilder withProject(int projectId) {
    params.put("bt_project_id", new SqlParameterValue(Types.INTEGER, projectId));
    return this;
  }
  
  public SqlParamsBuilder withCreateDate() {
    params.put("create_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE,
        DateTimeUtil.getCurrentUTC()));
    return this;
  }
  
  public SqlParamsBuilder withInteger(String name, int value) {
    params.put(name, new SqlParameterValue(Types.INTEGER, value));
    return this;
  }
  
  public SqlParamsBuilder withOther(String name, Object value) {
    params.put(name, new SqlParameterValue(Types.OTHER, value));
    return this;
  }
  
  public SqlParamsBuilder withVarchar(String name, String value) {
    params.put(name, new SqlParameterValue(Types.VARCHAR, value));
    return this;
  }
  
  public SqlParamsBuilder withBoolean(String name, boolean value) {
    params.put(name, new SqlParameterValue(Types.BOOLEAN, value));
    return this;
  }
  
  public SqlParamsBuilder withTimestampTimezone(String name, OffsetDateTime value) {
    params.put(name, new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE, value));
    return this;
  }
  
  public SqlParamsBuilder withBigint(String name, long value) {
    params.put(name, new SqlParameterValue(Types.BIGINT, value));
    return this;
  }
  
  public SqlParamsBuilder withArray(String name, Object[] value,
                             @SuppressWarnings("SameParameterValue") JDBCType elementType) {
    params.put(name, new SqlParameterValue(Types.ARRAY, elementType.getName()
        , new ArraySqlTypeValue(value)));
    return this;
  }
  
  public SqlParameterSource build() {
    return new MapSqlParameterSource(params);
  }
}
