package com.zylitics.btbr.service;

import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

public abstract class AbstractRepoFetcher {
  
  final WebClient.Builder webClientBuilder;
  final int projectId;
  
  AbstractRepoFetcher(WebClient.Builder webClientBuilder, int projectId) {
    this.webClientBuilder = webClientBuilder;
    this.projectId = projectId;
  }
  
  public abstract String fetchRepoAndReturnLocalPath() throws IOException;
}
