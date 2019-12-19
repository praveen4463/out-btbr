package com.zylitics.btbr.dao;

import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.TestCommand;
import com.zylitics.btbr.runner.TestCommandProvider;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class DaoTestCommandProvider implements TestCommandProvider {
  
  private final APICoreProperties apiCoreProperties;
  private final NamedParameterJdbcTemplate jdbc;
  
  @Autowired
  DaoTestCommandProvider(APICoreProperties apiCoreProperties,
                                NamedParameterJdbcTemplate jdbc) {
    this.apiCoreProperties = apiCoreProperties;
    this.jdbc = jdbc;
  }
  
  // nextPageToken is a sql condition without parameters that should be ready to be appended to a
  // statement (using WHERE or AND). It's generate and used internally for this class.
  @Override
  public Optional<TestCommandProvider.Result> getTestCommands(int testVersionId,
                                                             @Nullable String nextPageToken) {
    APICoreProperties.Runner runner = apiCoreProperties.getRunner();
    int maxSelect = runner.getMaxTestCommandLoad();
    String sql = "SELECT bt_test_command_id, command, target, value FROM bt_test_command" +
        " WHERE bt_test_version_id = :bt_test_version_id";
    
    if (!Strings.isNullOrEmpty(nextPageToken)) {
      sql = String.format("%s AND %s", sql, nextPageToken);
    }
    
    // we'll get max + 1 to know if there are more commands available thus requiring a token.
    sql = String.format("%s ORDER BY bt_test_command_id LIMIT %s;", sql, maxSelect + 1);
    
    SqlParameterSource namedParams = new MapSqlParameterSource("bt_test_version_id"
        , new SqlParameterValue(Types.INTEGER, testVersionId));
    
    List<TestCommand> commands = jdbc.query(sql, namedParams, (rs, rowNum) ->
        new TestCommand()
            .setTestCommandId(rs.getLong("bt_test_command_id"))
            .setTestVersionId(testVersionId)
            .setCommand(rs.getString("command"))
            .setTarget(rs.getString("target"))
            .setValue(rs.getString("value")));
    
    if (commands.size() == 0) {
      return Optional.empty();
    }
  
    String newNextPageToken = null;
    if (commands.size() > maxSelect) {
      // we need to generate a new nextPageToken
      long last = commands.get(maxSelect).getTestCommandId();
      // we've ordered by bt_test_command_id, thus if we've seen records till a particular
      // bt_test_command_id, future records should've greater bt_test_command_id, hence following
      // sql works.
      newNextPageToken = String.format("bt_test_command_id >= %s", last);
      // remove last command as we've done for what it was selected.
      commands.remove(maxSelect);
    }
    
    return Optional.of(new Result(commands, newNextPageToken));
  }
  
  private static class Result implements TestCommandProvider.Result {
    
    private final List<TestCommand> testCommands;
    
    private final String nextPageToken;
    
    private Result(List<TestCommand> testCommands, String nextPageToken) {
      this.testCommands = testCommands;
      this.nextPageToken = nextPageToken;
    }
  
    @Override
    public List<TestCommand> getTestCommands() {
      return testCommands;
    }
  
    @Override
    public String getNextPageToken() {
      return nextPageToken;
    }
  }
}
