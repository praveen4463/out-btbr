package com.zylitics.btbr.runner;

import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildWdSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Updates the db upon build completion.
 */
@Component
public class DbUpdateBuildCompleteCallback implements BuildCompleteCallback {
  
  private static final Logger LOG = LoggerFactory.getLogger(DbUpdateBuildCompleteCallback.class);
  
  private final TransactionTemplate transactionTemplate;
  private final BuildWdSessionProvider buildWdSessionProvider;
  private final BuildProvider buildProvider;
  
  @Autowired
  DbUpdateBuildCompleteCallback(TransactionTemplate transactionTemplate,
                                BuildWdSessionProvider buildWdSessionProvider,
                                BuildProvider buildProvider) {
    this.transactionTemplate = transactionTemplate;
    this.buildWdSessionProvider = buildWdSessionProvider;
    this.buildProvider = buildProvider;
  }
  
  @Override
  public void onBuildComplete(Build build, BuildWdSession buildWdSession) {
    try {
      transactionTemplate.executeWithoutResult(transactionStatus -> {
        // Throw runtime exceptions on problems, transaction will automatically rollbacks in case
        // of Runtime exception (default behavior). Note that all jdbcTemplate operations throw
        // a Runtime exception as well by translating SqlExceptions thus we don't have to worry
        // about any checked exceptions that don't rollback this transaction (default behavior)
        int updatedBuildWdSession = buildWdSessionProvider.updateBuildWdSession(buildWdSession);
        if (updatedBuildWdSession == 0) {
          throw new RuntimeException("Couldn't update bt_build_wd_session on build completion for" +
              " row " + buildWdSession);
        }
        int updatedBuild = buildProvider.updateBuild(build);
        if (updatedBuild == 0) {
          throw new RuntimeException("Couldn't update bt_build on build completion for" +
              " row " + build);
        }
      });
    } catch (Throwable t) {
      // catch any type of exception rethrown by transaction, as we shouldn't throw from this
      // callback.
      LOG.error(t.getMessage(), t);
    }
  }
}
