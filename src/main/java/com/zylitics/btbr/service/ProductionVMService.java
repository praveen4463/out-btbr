package com.zylitics.btbr.service;

import com.google.common.base.Charsets;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildVM;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

public class ProductionVMService implements VMService {
  
  private static final int RESPONSE_TIMEOUT_MIN = 2;
  
  private final WebClient webClient;
  
  public ProductionVMService(WebClient.Builder webClientBuilder,
                             APICoreProperties apiCoreProperties,
                             SecretsManager secretsManager) {
    APICoreProperties.Runner runner = apiCoreProperties.getRunner();
    String secret = secretsManager.getSecretAsPlainText(runner.getWzgpAuthSecretCloudFile());
  
    reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create()
        .responseTimeout(Duration.ofMinutes(RESPONSE_TIMEOUT_MIN));
    this.webClient = webClientBuilder
        .baseUrl(runner.getWzgpEndpoint() + "/" + runner.getWzgpVersion())
        .defaultHeaders(httpHeaders ->
            httpHeaders.setBasicAuth(HttpHeaders.encodeBasicAuth(runner.getWzgpAuthUser(),
                secret, Charsets.UTF_8)))
        .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }
  
  @Override
  public void setVMAsAvailable(BuildVM buildVM) {
    sendRequest(uriBuilder -> uriBuilder
        .path(getEndpoint(buildVM))
        .queryParam("requireRunningVM", "true")
        .build());
  }
  
  @Override
  public void deleteVM(BuildVM buildVM) {
    sendRequest(uriBuilder -> uriBuilder
        .path(getEndpoint(buildVM))
        .build());
  }
  
  private String getEndpoint(BuildVM buildVM) {
    return String.format("/zones/%s/grids/%s", buildVM.getZone(), buildVM.getName());
  }
  
  private void sendRequest(Function<UriBuilder, URI> uriFunction) {
    ResponseEntity<Void> response = webClient.delete()
        .uri(uriFunction)
        .retrieve()
        .toBodilessEntity()
        .retryWhen(Retry.backoff(5, Duration.ofMillis(500)))
        .block();
    Objects.requireNonNull(response);
    if (response.getStatusCode() != HttpStatus.OK) {
      throw new RuntimeException("Invalid response");
    }
  }
}
