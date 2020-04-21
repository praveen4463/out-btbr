package com.zylitics.btbr.dao;

import com.zylitics.btbr.model.BuildVM;
import com.zylitics.btbr.runner.provider.BuildVMProvider;
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
public class DaoBuildVMProvider extends AbstractDaoProvider implements BuildVMProvider {
  
  @Autowired
  DaoBuildVMProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int updateDeleteDate(BuildVM buildVM) {
    String sql = "UPDATE bt_build_vm SET delete_date = :delete_date" +
        " WHERE bt_build_id = :bt_build_id";
    
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(2));
    params.put("delete_date", new SqlParameterValue(Types.TIME_WITH_TIMEZONE,
        buildVM.getDeleteDate()));
    params.put("bt_build_id", new SqlParameterValue(Types.INTEGER, buildVM.getBuildId()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
    return jdbc.update(sql, namedParams);
  }
}
