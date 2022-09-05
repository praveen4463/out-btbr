package com.zylitics.btbr.service;

import com.zylitics.btbr.model.GithubConfig;
import com.zylitics.btbr.runner.provider.GithubConfigProvider;
import com.zylitics.btbr.webdriver.Configuration;
import org.openqa.selenium.io.Zip;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;

public class GithubRepoFetcher extends AbstractRepoFetcher {
  
  GithubConfigProvider configProvider;
  
  private final WebClient.Builder webClientBuilder;
  
  public GithubRepoFetcher(WebClient.Builder webClientBuilder,
                           GithubConfigProvider configProvider,
                           int organizationId) {
    super(webClientBuilder, organizationId);
    this.configProvider = configProvider;
  
    reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create()
        .responseTimeout(Duration.ofSeconds(30)).followRedirect(true);
    this.webClientBuilder = webClientBuilder
        .baseUrl("https://api.github.com/repos")
        .clientConnector(new ReactorClientHttpConnector(httpClient));
  }
  
  // https://docs.github.com/en/rest/repos/contents#download-a-repository-archive-zip
  @Override
  public String fetchRepoAndReturnLocalPath() throws IOException {
    GithubConfig config = configProvider.getGithubConfig(organizationId)
        .orElseThrow(() ->
            new RuntimeException("No git config is attached to org: " + organizationId));
    Path repoArchiveLocalPath =
        Paths.get(Configuration.SYS_DEF_TEMP_DIR, config.getRepoName(), ".zip");
    Path repoDirLocalPath =
        Files.createDirectory(Paths.get(Configuration.SYS_DEF_TEMP_DIR, config.getRepoName()));
    
    webClientBuilder.defaultHeaders(httpHeaders -> {
      httpHeaders.setAccept(
          Collections.singletonList(new MediaType("application", "vnd.github+json")));
      httpHeaders.set("Authorization", "token " + config.getApiToken());
    });
  
    Flux<DataBuffer> dataBufferFlux = webClientBuilder.build().get()
        .uri(uriBuilder -> uriBuilder
            .pathSegment(config.getRepoOwner())
            .pathSegment(config.getRepoName())
            .pathSegment("zipball")
            .pathSegment(config.getMainBranchName()).build())
        .retrieve()
        .bodyToFlux(DataBuffer.class);
    DataBufferUtils.write(dataBufferFlux, repoArchiveLocalPath, StandardOpenOption.CREATE_NEW).
        block();
    // Using selenium's Zip utility as it looks good, and I don't want to write a function/use a lib
    try (InputStream repoArchiveStream = Files.newInputStream(repoArchiveLocalPath)) {
      Zip.unzip(repoArchiveStream, repoDirLocalPath.toFile());
    }
    // When unarchiving the zip file, there will be a root dir named like <repoOwner-repoName-...>
    // We need to get that dir to work with.
    Path unarchivedDir;
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(repoDirLocalPath,
        (file) -> file.getFileName().toString().startsWith(config.getRepoOwner())))  {
      Iterator<Path> pathIterator = dirStream.iterator();
      if (!pathIterator.hasNext()) {
        throw new RuntimeException("Looks like there is some problem in unzipping as no inner dir" +
            " was found.");
      }
      unarchivedDir = pathIterator.next();
    }
    return unarchivedDir.toAbsolutePath().toString();
  }
}
