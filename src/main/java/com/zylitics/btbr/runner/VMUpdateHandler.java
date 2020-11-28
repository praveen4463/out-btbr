package com.zylitics.btbr.runner;

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.CharStreams;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildVM;
import com.zylitics.btbr.runner.provider.BuildVMProvider;
import com.zylitics.btbr.runner.provider.BuildVMUpdateDeleteDate;
import com.zylitics.btbr.util.DateTimeUtil;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
class VMUpdateHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(VMUpdateHandler.class);
  private static final String AUTHORIZATION = "Authorization";
  private static final Json JSON = new Json();
  private static final String WZGP_API_BASE_PATH = "/zones/ZONE/grids/NAME";
  
  private final APICoreProperties apiCoreProperties;
  private final SecretsManager secretsManager;
  private final BuildVMProvider buildVMProvider;
  private final HttpClient.Factory httpClientFactory;
  
  VMUpdateHandler(APICoreProperties apiCoreProperties,
                  SecretsManager secretsManager,
                  BuildVMProvider buildVMProvider) {
    this.apiCoreProperties = apiCoreProperties;
    this.secretsManager = secretsManager;
    this.buildVMProvider = buildVMProvider;
    this.httpClientFactory = HttpClient.Factory.createDefault();
  }
  
  // log any exception and don't throw.
  void update(Build build) {
    int buildVMId = build.getBuildVMId();
    BuildVM buildVM = buildVMProvider.getBuildVM(buildVMId)
        .orElseThrow(() -> new RuntimeException("Couldn't get buildVM for " + buildVMId));
    APICoreProperties.Runner runner = apiCoreProperties.getRunner();
    try {
      HttpClient client = httpClientFactory.createClient(
          new URL(runner.getWzgpEndpoint() + "/" + runner.getWzgpVersion())
      );
      if (!buildVM.isDeleteFromRunner()) {
        // when not deleting, update availability status label at instance.
        HttpRequest request = new HttpRequest(HttpMethod.POST, getApiPath(buildVM));
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> labels = new HashMap<>();
        Map<String, Object> buildProperties = new HashMap<>();
        labels.put(runner.getLabelKeyAvailabilityStatus(),
            runner.getLabelValueAvailabilityStatus());
        buildProperties.put("buildId", build.getBuildId());
        requestBody.put("labels", labels);
        requestBody.put("buildProperties", buildProperties);
        byte[] payload = JSON.toJson(requestBody).getBytes(UTF_8);
        request.setHeader(CONTENT_TYPE, JSON_UTF_8.toString());
        request.setHeader(CONTENT_LENGTH, String.valueOf(payload.length));
        request.setContent(() -> new ByteArrayInputStream(payload));
        execute(client, request);
        return;
      }
      LOG.debug("updating vm deleteDate for buildVMId {}", buildVMId);
      int result = buildVMProvider.updateDeleteDate(new BuildVMUpdateDeleteDate(buildVMId,
          DateTimeUtil.getCurrentUTC()));
      if (result != 1) {
        LOG.error("VM deleteDate couldn't be updated for buildVMId {}", buildVMId);
      }
      LOG.debug("going to delete vm for buildVMId {}", buildVMId);
      execute(client, new HttpRequest(HttpMethod.DELETE, getApiPath(buildVM)));
    } catch (Throwable t) {
      LOG.error("Failing to free vm for buildVMId " + buildVMId, t);
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
  
  private String getApiPath(BuildVM buildVM) {
    return WZGP_API_BASE_PATH.replace("ZONE", buildVM.getZone())
        .replace("NAME", buildVM.getName());
  }
  
  private void execute(HttpClient client, HttpRequest request) throws Exception {
    secretsManager.reAcquireClientAfterClose(); // the client got closed in Launcher itself.
    String secret = secretsManager.getSecretAsPlainText(apiCoreProperties.getRunner()
        .getWzgpAuthSecretCloudFile());
    String authHeader = Base64.getEncoder().encodeToString((apiCoreProperties
        .getRunner().getWzgpAuthUser() + ":" + secret).getBytes());
    request.setHeader(AUTHORIZATION, authHeader);
    HttpResponse response = client.execute(request);
    if (response.getStatus() != 200) {
      Map<String, Object> responseBody = getBodyFromResponse(response);
      throw new RuntimeException("Response status: " + responseBody.get("status") + ", code: " +
          responseBody.get("httpStatusCode") +  "error:" + responseBody.get("error"));
    }
  }
}
