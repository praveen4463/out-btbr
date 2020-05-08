package com.zylitics.btbr.runner;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.zylitics.btbr.SecretsManager;
import com.zylitics.btbr.config.APICoreProperties;
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

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import static java.nio.charset.StandardCharsets.UTF_8;

class VMDeleteHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(VMDeleteHandler.class);
  private static final String AUTHORIZATION = "Authorization";
  private static final Json JSON = new Json();
  
  private final APICoreProperties apiCoreProperties;
  private final SecretsManager secretsManager;
  private final BuildVMProvider buildVMProvider;
  private final HttpClient.Factory httpClientFactory;
  
  VMDeleteHandler(APICoreProperties apiCoreProperties,
                  SecretsManager secretsManager,
                  BuildVMProvider buildVMProvider) {
    this(apiCoreProperties,
        secretsManager,
        buildVMProvider,
        HttpClient.Factory.createDefault());
  }
  
  VMDeleteHandler(APICoreProperties apiCoreProperties,
                  SecretsManager secretsManager,
                  BuildVMProvider buildVMProvider,
                  HttpClient.Factory httpClientFactory) {
    this.apiCoreProperties = apiCoreProperties;
    this.secretsManager = secretsManager;
    this.buildVMProvider = buildVMProvider;
    this.httpClientFactory = httpClientFactory;
  }
  
  /**
   * Deletes this VM using the provided fully qualified URL to deletion api. It updates bt_build_vm
   * tables with the deletion date time before doing so.
   * @param vmDeleteUrl The fully qualified URL to deletion api
   */
  // buildVMId is marked as Nullable so that even if some problem occurs during Build fetch, we can
  // still delete VM.
  // log any exception and don't throw.
  void delete(@Nullable Integer buildVMId, String vmDeleteUrl) {
    if (Strings.isNullOrEmpty(vmDeleteUrl)) {
      LOG.warn("No vm delete url is given, this VM will not be deleted from here");
      return;
    }
    try {
      if (buildVMId != null) {
        LOG.debug("updating vm deleteDate for buildVMId {}", buildVMId);
        // update the deletion date, it's update before vm is actually deleted because we require
        // the deletion date to bill customer, if vm couldn't be deleted after date after, it's our
        // bug, customer has freed the vm until now.
        buildVMProvider.updateDeleteDate(new BuildVMUpdateDeleteDate(buildVMId,
            DateTimeUtil.getCurrentUTC()));
      } else {
        LOG.error("buildVMId is null while attempting to delete vm, can't set delete date");
      }
      
      LOG.debug("going to delete the VM using the given URL");
      // now delete VM, throw exception if response is not 200 OK
      secretsManager.reAcquireClientAfterClose(); // the client got closed in Launcher itself.
      String secret = secretsManager.getSecretAsPlainText(apiCoreProperties.getRunner()
          .getWzgpAuthSecretCloudFile());
      String authHeader = Base64.getEncoder().encodeToString((apiCoreProperties
          .getRunner().getWzgpAuthUser() + ":" + secret).getBytes());
      
      HttpClient client = httpClientFactory.createClient(new URL(vmDeleteUrl));
      HttpRequest request = new HttpRequest(HttpMethod.DELETE, "");
      request.setHeader(AUTHORIZATION, authHeader);
      HttpResponse response = client.execute(request);
      if (response.getStatus() != 200) {
        Map<String, Object> responseBody = getBodyFromResponse(response);
        throw new RuntimeException("Response status: " + responseBody.get("status") + ", code: " +
            responseBody.get("httpStatusCode") +  "error:" + responseBody.get("error"));
      }
    } catch (Throwable t) {
      LOG.error("Couldn't delete the VM at " + vmDeleteUrl + " for buildVMId " + buildVMId, t);
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
