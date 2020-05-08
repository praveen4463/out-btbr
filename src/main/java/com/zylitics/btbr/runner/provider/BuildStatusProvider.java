package com.zylitics.btbr.runner.provider;

public interface BuildStatusProvider {
  
  /** Saves into BuildStatus when a {@link com.zylitics.btbr.model.TestVersion} starts */
  int saveOnStart(BuildStatusSaveOnStart buildStatusSaveOnStart);
  
  /** Saves into BuildStatus when a {@link com.zylitics.btbr.model.TestVersion} can't start */
  int saveWontStart(BuildStatusSaveWontStart buildStatusSaveWontStart);
  
  /** Updates only the zwl-line-number using build and testVersion.*/
  int updateLine(BuildStatusUpdateLine buildStatusUpdateLine);
  
  /** Updates BuildStatus when a {@link com.zylitics.btbr.model.TestVersion} ends */
  int updateOnEnd(BuildStatusUpdateOnEnd buildStatusUpdateOnEnd);
}
