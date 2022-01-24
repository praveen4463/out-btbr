package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.File;
import com.zylitics.btbr.model.Test;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.runner.provider.TestVersionProvider;
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
public class DaoTestVersionProvider extends AbstractDaoProvider implements TestVersionProvider {
  
  @Autowired
  DaoTestVersionProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<List<TestVersion>> getTestVersions(int buildId) {
    Preconditions.checkArgument(buildId > 0, "buildId is required");

    String sql = "SELECT" +
        " bt_test_version_id" +
        ", bt_test_version_name" +
        ", bt_test_version_code" +
        ", bt_test_id" +
        ", bt_test_name" +
        ", bt_file_id" +
        ", bt_file_name" +
        " FROM bt_build_tests" +
        " WHERE bt_build_id = :bt_build_id ORDER BY bt_build_tests_id;";

    SqlParameterSource namedParams = new MapSqlParameterSource("bt_build_id",
        new SqlParameterValue(Types.INTEGER, buildId));

    List<TestVersion> versions = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new TestVersion()
            .setTestVersionId(rs.getInt("bt_test_version_id"))
            .setName(rs.getString("bt_test_version_name"))
            .setCode(rs.getString("bt_test_version_code"))
            .setTest(new Test()
                .setTestId(rs.getInt("bt_test_id"))
                .setName(rs.getString("bt_test_name")))
            .setFile(new File()
                .setFileId(rs.getInt("bt_file_id"))
                .setName(rs.getString("bt_file_name"))));

    if (versions.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(versions);
  }

  @Override
  public Optional<TestVersion> getTestVersion(int projectId,
                                              String fileName,
                                              String testName,
                                              String versionName) {
    String sql = "SELECT v.bt_test_version_id vid, v.name vn, v.code vc, t.bt_test_id tid, t.name tn\n" +
        ", f.bt_file_id fid, f.name fn\n" +
        "FROM bt_file AS f\n" +
        "INNER JOIN bt_test AS t ON (f.bt_file_id = t.bt_file_id)\n" +
        "INNER JOIN bt_test_version AS v ON (t.bt_test_id = v.bt_test_id)\n" +
        "WHERE bt_project_id = :bt_project_id and f.name = :fn and t.name = :tn and v.name = :vn";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(4));
    params.put("bt_project_id", new SqlParameterValue(Types.INTEGER, projectId));
    params.put("fn", new SqlParameterValue(Types.VARCHAR, fileName));
    params.put("tn", new SqlParameterValue(Types.VARCHAR, testName));
    params.put("vn", new SqlParameterValue(Types.VARCHAR, versionName));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);

    List<TestVersion> versions = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new TestVersion()
            .setTestVersionId(rs.getInt("vid"))
            .setName(rs.getString("vn"))
            .setCode(rs.getString("vc"))
            .setTest(new Test()
                .setTestId(rs.getInt("tid"))
                .setName(rs.getString("tn")))
            .setFile(new File()
                .setFileId(rs.getInt("fid"))
                .setName(rs.getString("fn"))));

    if (versions.size() != 1) {
      return Optional.empty();
    }
    return Optional.of(versions.get(0));
  }
}
