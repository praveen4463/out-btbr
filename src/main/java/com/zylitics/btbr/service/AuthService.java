package com.zylitics.btbr.service;

public interface AuthService {
  
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  boolean isAuthorized(String authHeader);
}
