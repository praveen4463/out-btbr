package com.zylitics.btbr.service;

import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;

import java.util.Arrays;
import java.util.Base64;

public class ProductionAuthService implements AuthService {
  
  private final String rootUserAuthHeader;
  
  public ProductionAuthService(APICoreProperties apiCoreProperties,
                               SecretsManager secretsManager) {
    APICoreProperties.Runner runner = apiCoreProperties.getRunner();
    String secret = secretsManager.getSecretAsPlainText(runner.getBtbrAuthSecretCloudFile());
    rootUserAuthHeader = Base64.getEncoder().encodeToString((runner.getBtbrAuthUser() + ":" +
        secret).getBytes());
  }
  
  @Override
  public boolean isAuthorized(String authHeader) {
    return rootUserAuthHeader.equals(authHeader);
  }
}
