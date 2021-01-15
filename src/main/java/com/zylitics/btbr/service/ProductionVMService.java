package com.zylitics.btbr.service;

import com.google.common.io.CharStreams;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildVM;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ProductionVMService implements VMService {
  
  private static final String AUTHORIZATION = "Authorization";
  private static final Json JSON = new Json();
  
  private final APICoreProperties.Runner runner;
  private final SecretsManager secretsManager;
  private final HttpClient.Factory httpClientFactory;
  
  public ProductionVMService(APICoreProperties apiCoreProperties,
                             SecretsManager secretsManager) {
    this.runner = apiCoreProperties.getRunner();
    this.secretsManager = secretsManager;
    this.httpClientFactory = HttpClient.Factory.createDefault();
  }
  
  @SuppressWarnings("UnstableApiUsage")
  @Override
  public void setVMAsAvailable(BuildVM buildVM, Build build) {
    try {
      HttpRequest request = new HttpRequest(HttpMethod.POST, "/setLabels");
      Map<String, Object> requestBody = new HashMap<>();
      Map<String, Object> labels = new HashMap<>();
      Map<String, Object> buildProperties = new HashMap<>();
      labels.put(runner.getLabelKeyAvailabilityStatus(), runner.getLabelValueAvailabilityStatus());
      buildProperties.put("buildId", build.getBuildId());
      requestBody.put("labels", labels);
      requestBody.put("buildProperties", buildProperties);
      byte[] payload = JSON.toJson(requestBody).getBytes(UTF_8);
      request.setHeader(CONTENT_TYPE, JSON_UTF_8.toString());
      request.setHeader(CONTENT_LENGTH, String.valueOf(payload.length));
      request.setContent(() -> new ByteArrayInputStream(payload));
      execute(getClient(buildVM), request);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
  
  @Override
  public void deleteVM(BuildVM buildVM) {
    try {
      execute(getClient(buildVM), new HttpRequest(HttpMethod.DELETE, ""));
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
  
  private HttpClient getClient(BuildVM buildVM) throws MalformedURLException {
    return httpClientFactory.createClient(new URL(String.format("%s/%s/zones/%s/grids/%s",
        runner.getWzgpEndpoint(), runner.getWzgpVersion(), buildVM.getZone(), buildVM.getName())));
  }
  
  private void execute(HttpClient client, HttpRequest request) throws Exception {
    try {
      secretsManager.reAcquireClientAfterClose(); // the client got closed in Launcher itself.
      String secret = secretsManager.getSecretAsPlainText(runner.getWzgpAuthSecretCloudFile());
      String authHeader = Base64.getEncoder().encodeToString((runner.getWzgpAuthUser() + ":" +
          secret).getBytes());
      request.setHeader(AUTHORIZATION, authHeader);
      HttpResponse response = client.execute(request);
      if (response.getStatus() != 200) {
        Map<String, Object> responseBody = getBodyFromResponse(response);
        throw new RuntimeException("Response status: " + responseBody.get("status") + ", code: " +
            responseBody.get("httpStatusCode") +  "error:" + responseBody.get("error"));
      }
    } finally {
      try {
        secretsManager.close();
      } catch (IOException ignore) {}
    }
  }
  
  private Map<String, Object> getBodyFromResponse(HttpResponse response) throws IOException {
    try (BufferedReader reader =
             new BufferedReader(new InputStreamReader(response.getContent().get(), UTF_8))) {
      String content = CharStreams.toString(reader);
      return JSON.toType(content, Json.MAP_TYPE);
    }
  }
}
