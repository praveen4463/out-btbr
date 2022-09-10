package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.GitProvider;
import com.zylitics.btbr.model.Project;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.service.AbstractRepoFetcher;
import com.zylitics.btbr.service.GithubRepoFetcher;
import com.zylitics.btbr.service.ZWLFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitTestVersionProvider implements TestVersionProvider {
  
  private final TestVersionForGitRunProvider testVersionForGitRunProvider;
  private final GithubConfigProvider githubConfigProvider;
  private final WebClient.Builder webClientBuilder;
  
  private ZWLFileReader zwlFileReader = null;
  
  @Autowired
  public GitTestVersionProvider(TestVersionForGitRunProvider testVersionForGitRunProvider,
                                GithubConfigProvider githubConfigProvider,
                                WebClient.Builder webClientBuilder) {
    this.testVersionForGitRunProvider = testVersionForGitRunProvider;
    this.githubConfigProvider = githubConfigProvider;
    this.webClientBuilder = webClientBuilder;
  }
  
  public void init(Project project) {
    int projectId = project.getProjectId();
    
    AbstractRepoFetcher abstractRepoFetcher;
    GitProvider gitProvider = project.getGitProvider();
    switch (gitProvider) {
      case GITHUB:
        abstractRepoFetcher = new GithubRepoFetcher(webClientBuilder,
            githubConfigProvider, projectId);
        break;
      default:
        throw new IllegalArgumentException(String.format(
            "Git provider %s is not currently implemented", gitProvider));
    }
    String zwlProjectDir;
    try {
      zwlProjectDir = abstractRepoFetcher.fetchRepoAndReturnLocalPath();
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
    this.zwlFileReader = new ZWLFileReader(zwlProjectDir);
  }
  
  @Override
  public Optional<List<TestVersion>> getTestVersions(int buildId) {
    Objects.requireNonNull(zwlFileReader, "invoke init first");
    
    Optional<List<TestVersion>> optionalTestVersions =
        testVersionForGitRunProvider.getTestVersionsWithoutCode(buildId);
    if (!optionalTestVersions.isPresent()) {
      return optionalTestVersions;
    }
    List<TestVersion> testVersions = optionalTestVersions.get();
    Set<String> files = testVersions.stream()
        .map(testVersion -> testVersion.getFile().getName())
        .collect(Collectors.toSet());
    Map<String, Map<String, String>> testToCodeByFile;
    try {
      testToCodeByFile = zwlFileReader.readFiles(files);
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
    
    setCodeToTestVersions(testVersions, testToCodeByFile);
    return optionalTestVersions;
  }
  
  private void setCodeToTestVersions(List<TestVersion> testVersions,
                                    Map<String, Map<String, String>> testToCodeByFile) {
    for (TestVersion testVersion : testVersions) {
      String fileName = testVersion.getFile().getName();
      if (!testToCodeByFile.containsKey(fileName)) {
        throw new IllegalArgumentException(
            String.format("File %s doesn't exist in the repo. Aborting run.", fileName));
      }
      
      String testName = testVersion.getTest().getName();
      Map<String, String> testToCode = testToCodeByFile.get(fileName);
      if (!testToCode.containsKey(testName)) {
        throw new IllegalArgumentException(
            String.format("Test %s doesn't exist in file %s in the repo. Aborting run.",
                testName, fileName));
      }
      
      // TODO: This could have problems with functions if they don't have any code. We should have
      //  latest value of a test to make sure user really referred to non-latest version. For now
      //  this is fine but if there are problems in future, check for latest value and if it's
      //  latest, let it go through.
      String code = testToCode.get(testName);
      if (code.replaceAll("[\\n\\t\\r\\s]", "").equals("")) {
        throw new IllegalArgumentException(
            String.format("Test %s of %s doesn't have code in the repo." +
                " It looks like the latest version of this test is empty and you're trying" +
                " to run some other version. When running from git, only the latest versions" +
                " of tests are supported.", testName, fileName));
      }
    
      testVersion.setCode(code);
    }
  }
  
  @Override
  public Optional<TestVersion> getTestVersion(int projectId,
                                              String fileName,
                                              String testName,
                                              String versionName) {
    Objects.requireNonNull(zwlFileReader, "invoke init first");
    
    Optional<TestVersion> optionalTestVersion = testVersionForGitRunProvider
        .getTestVersionWithoutCode(projectId, fileName, testName, versionName);
    if (!optionalTestVersion.isPresent()) {
      return optionalTestVersion;
    }
    TestVersion testVersion = optionalTestVersion.get();
    Map<String, Map<String, String>> testToCodeByFile;
    try {
      testToCodeByFile = zwlFileReader.readFile(fileName);
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
  
    setCodeToTestVersions(Collections.singletonList(testVersion), testToCodeByFile);
    return optionalTestVersion;
  }
}
