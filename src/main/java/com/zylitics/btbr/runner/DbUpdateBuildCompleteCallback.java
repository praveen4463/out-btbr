package com.zylitics.btbr.runner;

import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.runner.provider.BuildProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Updates the db upon build completion.
 */
@Component
class DbUpdateBuildCompleteCallback implements BuildCompleteCallback {
  
  private static final Logger LOG = LoggerFactory.getLogger(DbUpdateBuildCompleteCallback.class);
  
  private final BuildProvider buildProvider;
  
  @Autowired
  DbUpdateBuildCompleteCallback(BuildProvider buildProvider) {
    this.buildProvider = buildProvider;
  }
  
  @Override
  public void onBuildComplete(Build build) {
    try {
      int updatedBuild = buildProvider.updateBuild(build);
      if (updatedBuild == 0) {
        throw new RuntimeException("Couldn't update bt_build on build completion for" +
            " row " + build);
      }
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }
}
