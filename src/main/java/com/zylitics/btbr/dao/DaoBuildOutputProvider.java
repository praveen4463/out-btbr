package com.zylitics.btbr.dao;

import com.zylitics.btbr.model.BuildOutput;
import com.zylitics.btbr.runner.provider.BuildOutputProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DaoBuildOutputProvider extends AbstractDaoProvider implements BuildOutputProvider {
  
  @Autowired
  DaoBuildOutputProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int newBuildOutput(BuildOutput buildOutput) {
    String sql = "INSERT INTO bt_build_output (bt_build_id, bt_test_version_id, output,\n" +
        "ended, create_date)\n" +
        "VALUES (:bt_build_id, :bt_test_version_id, :output, :ended, :create_date)";
    return jdbc.update(sql, new SqlParamsBuilder()
        .withInteger("bt_build_id", buildOutput.getBuildId())
        .withInteger("bt_test_version_id", buildOutput.getTestVersionId())
        .withOther("output", buildOutput.getOutput())
        .withBoolean("ended", buildOutput.isEnded())
        .withCreateDate().build());
  }
}
