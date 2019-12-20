package com.zylitics.btbr.runner;

public interface BulkSaveProvider<T> {
  
  /**
   * submits the given object of type {@link T} for a later bulk operation, this method is
   * non-blocking.
   * @param obj an object to be submitted for later bulk save.
   * @throws RuntimeException if there were problems submitting to bulk
   */
  void saveAsync(T obj) throws RuntimeException;
  
  /**
   * Should be invoked just before the completion of build to process unsaved submitted records.
   * This method blocks until completion.
   * @throws RuntimeException if there were problems processing the remaining in bulk
   */
  void processRemainingAndTearDown() throws RuntimeException;
  
  /**
   * When the bulk processor is turned down due to some problem, future requests for submission or
   * processing may not be accepted, specifies whether to throw an exception or just return silently
   * @return boolean indicating whether to throw an exception or just return silently.
   */
  boolean throwIfTurnedDown();
  
  /**
   * When a request resulted in an exception, whether to throw that exception or just return
   * silently and log error. Returning silently may be desirable when the caller don't want to
   * handle failures itself.
   * @return boolean indicating whether to throw an exception or just return silently.
   */
  boolean throwOnException();
}
