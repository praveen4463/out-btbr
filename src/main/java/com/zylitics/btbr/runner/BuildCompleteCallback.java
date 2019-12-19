package com.zylitics.btbr.runner;

import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildWdSession;

/**
 * Invoked by async request handler whenever a running build is failed or succeed.
 * Note: Implementations are not expected to throw any exceptions and handle intrinsically. Used
 * only in 'async' class of methods.
 */
// Note: Autowire all implementations using a list type in main handler class and use only in async
// methods.
public interface BuildCompleteCallback {
  
  void onBuildComplete(Build build, BuildWdSession buildWdSession);
}
