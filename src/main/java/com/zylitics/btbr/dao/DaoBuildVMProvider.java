package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.BuildVM;
import com.zylitics.btbr.runner.provider.BuildVMProvider;
import com.zylitics.btbr.runner.provider.BuildVMUpdateDeleteDate;
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
public class DaoBuildVMProvider extends AbstractDaoProvider implements BuildVMProvider {
  
  @Autowired
  DaoBuildVMProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<BuildVM> getBuildVM(int buildVMId) {
    Preconditions.checkArgument(buildVMId > 0, "buildVMId is required");
    String sql = "SELECT" +
        " name" +
        ", zone" +
        ", delete_from_runner" +
        " FROM bt_build_vm WHERE bt_build_vm_id = :bt_build_vm_id;";
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_vm_id",
        new SqlParameterValue(Types.INTEGER, buildVMId));
    List<BuildVM> buildVM = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new BuildVM()
            .setName(rs.getString("name"))
            .setZone(rs.getString("zone"))
            .setDeleteFromRunner(rs.getBoolean("delete_from_runner")));
    if (buildVM.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(buildVM.get(0));
  }
  
  @Override
  public int updateDeleteDate(BuildVMUpdateDeleteDate buildVMUpdateDeleteDate) {
    Preconditions.checkNotNull(buildVMUpdateDeleteDate, "buildVMUpdateDeleteDate can't be null");
    
    String sql = "UPDATE bt_build_vm SET delete_date = :delete_date" +
        " WHERE bt_build_vm_id = :bt_build_vm_id";
    
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(2));
    params.put("delete_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE,
        buildVMUpdateDeleteDate.getDeleteDate()));
    params.put("bt_build_vm_id", new SqlParameterValue(Types.INTEGER,
        buildVMUpdateDeleteDate.getBuildVMId()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
    return jdbc.update(sql, namedParams);
  }
}
