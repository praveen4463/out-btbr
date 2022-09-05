package com.zylitics.btbr.service;

import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

public abstract class AbstractRepoFetcher {
  
  final WebClient.Builder webClientBuilder;
  final int organizationId;
  
  AbstractRepoFetcher(WebClient.Builder webClientBuilder, int organizationId) {
    this.webClientBuilder = webClientBuilder;
    this.organizationId = organizationId;
  }
  
  public abstract String fetchRepoAndReturnLocalPath() throws IOException;
}
