package com.zylitics.btbr.service;

import com.google.common.io.CharStreams;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.BuildVM;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ProductionVMService implements VMService {
  
  private static final String AUTHORIZATION = "Authorization";
  private static final Json JSON = new Json();
  
  private final APICoreProperties.Runner runner;
  private final String wzgpUserAuthHeader;
  private final HttpClient.Factory httpClientFactory;
  
  public ProductionVMService(APICoreProperties apiCoreProperties,
                             SecretsManager secretsManager) {
    this.runner = apiCoreProperties.getRunner();
    String secret = secretsManager.getSecretAsPlainText(runner.getWzgpAuthSecretCloudFile());
    wzgpUserAuthHeader = Base64.getEncoder().encodeToString((runner.getWzgpAuthUser() + ":" +
        secret).getBytes());
    this.httpClientFactory = HttpClient.Factory.createDefault();
  }
  
  @Override
  public void setVMAsAvailable(BuildVM buildVM) {
    try {
      HttpRequest request = new HttpRequest(HttpMethod.DELETE, "");
      request.addQueryParameter("requireRunningVM", "true");
      // sending this will tell api to keep it running but mark as available.
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
    request.setHeader(AUTHORIZATION, wzgpUserAuthHeader);
    HttpResponse response = client.execute(request);
    if (response.getStatus() != 200) {
      Map<String, Object> responseBody = getBodyFromResponse(response);
      throw new RuntimeException("Response status: " + responseBody.get("status") + ", code: " +
          responseBody.get("httpStatusCode") +  "error:" + responseBody.get("error"));
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
