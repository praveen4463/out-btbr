package com.zylitics.btbr.dao;

import com.google.common.base.Preconditions;
import com.zylitics.btbr.model.GithubConfig;
import com.zylitics.btbr.runner.provider.GithubConfigProvider;
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
class DaoGithubConfigProvider extends AbstractDaoProvider implements GithubConfigProvider {
  
  @Autowired
  DaoGithubConfigProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<GithubConfig> getGithubConfig(int projectId) {
    Preconditions.checkArgument(projectId > 0, "projectId is required");
    
    String sql = "SELECT api_token, repo_owner, repo_name, main_branch_name\n" +
        "FROM github_config\n" +
        "WHERE project_id = :project_id;";
  
    SqlParameterSource namedParams = new MapSqlParameterSource("project_id",
        new SqlParameterValue(Types.INTEGER, projectId));
  
    List<GithubConfig> configs = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new GithubConfig()
            .setApiToken(rs.getString("api_token"))
            .setRepoOwner(rs.getString("repo_owner"))
            .setRepoName(rs.getString("repo_name"))
            .setMainBranchName(rs.getString("main_branch_name")));
  
    if (configs.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(configs.get(0));
  }
}
