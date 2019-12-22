package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.TestCommand;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface TestCommandProvider {
  
  default Optional<Result> getTestCommands(int testVersionId) {
    return getTestCommands(testVersionId, null);
  }
  
  /**
   *
   * @param testVersionId represents bt_test_version.bt_test_version_id
   * @param nextPageToken token to be used in subsequent calls to fetch next page of commands
   * @return {@link Result}
   */
  Optional<Result> getTestCommands(int testVersionId, @Nullable String nextPageToken);
  
  interface Result {
    
    List<TestCommand> getTestCommands();
  
    /**
     * when total commands returned exceeds {@link com.zylitics.btbr.config.APICoreProperties.Runner#getMaxTestCommandLoad()},
     * this token will be returned to caller. Caller should store and send it in subsequent calls
     * to get next page of commands.
     * @return token to be used in subsequent calls to fetch next page of commands
     */
    String getNextPageToken();
  }
}
