package com.zylitics.btbr.service;

public class LocalAuthService implements AuthService {
  
  @Override
  public boolean isAuthorized(String authHeader) {
    String localBtbrAutSecret = System.getenv("LOCAL_BTBR_AUTH_SECRET");
    if (localBtbrAutSecret == null) {
      localBtbrAutSecret = "local";
    }
    return authHeader.equals(localBtbrAutSecret);
  }
}
