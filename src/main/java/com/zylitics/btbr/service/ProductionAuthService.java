package com.zylitics.btbr.service;

import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;

import java.util.Arrays;
import java.util.Base64;

public class ProductionAuthService implements AuthService {
  
  private final byte[] rootUserAuthHeader;
  
  public ProductionAuthService(APICoreProperties apiCoreProperties,
                                SecretsManager secretsManager) {
    APICoreProperties.Runner runner = apiCoreProperties.getRunner();
    String secret = secretsManager.getSecretAsPlainText(runner.getBtbrAuthSecretCloudFile());
    rootUserAuthHeader = Base64.getEncoder().encode((runner.getBtbrAuthUser() + ":" +
        secret).getBytes());
  }
  
  @Override
  public boolean isAuthorized(String authHeader) {
    return Arrays.equals(rootUserAuthHeader, Base64.getDecoder().decode(authHeader));
  }
}
