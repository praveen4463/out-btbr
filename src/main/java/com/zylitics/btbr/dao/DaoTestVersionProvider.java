package com.zylitics.btbr.dao;

import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.model.ZwlProgram;
import com.zylitics.btbr.runner.provider.TestVersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class DaoTestVersionProvider extends AbstractDaoProvider implements TestVersionProvider {
  
  @Autowired
  DaoTestVersionProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<List<TestVersion>> getTestVersion(int buildId) {
    String sql = "SELECT" +
        " bt.bt_test_version_id" +
        " , zp.code" +
        " FROM bt_build_tests AS bt INNER JOIN bt_test_version AS tv" +
        " ON (bt.bt_test_version_id = tv.bt_test_version_id)" +
        " INNER JOIN zwl_program AS zp" +
        " ON (tv.zwl_program_id = zp.zwl_program_id)" +
        " WHERE bt.bt_build_id = :bt_build_id;";
  
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));
    
    List<TestVersion> versions = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new TestVersion()
            .setTestVersionId(rs.getInt("bt_test_version_id"))
            .setZwlProgram(new ZwlProgram()
                .setCode(rs.getString("code"))));
    
    if (versions.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(versions);
  }
}
