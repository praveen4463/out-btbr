package com.zylitics.btbr.service;

import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;

public class LocalAuthService extends ProductionAuthService implements AuthService {
  
  // e2e tests can skip auth when needed.
  private static final String NO_AUTH_SYS_PROP = "zl.btbr.e2e.noAuth";
  
  public LocalAuthService(APICoreProperties apiCoreProperties,
                          SecretsManager secretsManager) {
    super(apiCoreProperties, secretsManager);
  }
  
  @Override
  public boolean isAuthorized(String authHeader) {
    boolean noAuth = Boolean.getBoolean(NO_AUTH_SYS_PROP);
    if (noAuth) {
      return true;
    }
    return super.isAuthorized(authHeader);
  }
}
