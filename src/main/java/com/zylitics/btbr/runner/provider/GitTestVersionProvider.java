package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.GitProvider;
import com.zylitics.btbr.model.Project;
import com.zylitics.btbr.model.TestVersion;
import com.zylitics.btbr.service.AbstractRepoFetcher;
import com.zylitics.btbr.service.GithubRepoFetcher;
import com.zylitics.btbr.service.ZWLFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitTestVersionProvider implements TestVersionProvider {
  
  private static final Logger LOG = LoggerFactory.getLogger(GitTestVersionProvider.class);
  
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
  
  public void init(Project project) throws IOException {
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
    String zwlProjectDir = abstractRepoFetcher.fetchRepoAndReturnLocalPath();
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
  
  private void resetCodeIfNotExistsInGit(TestVersion testVersion) {
    testVersion.setCode("print(\"ERROR: This test doesn't exists in git\")");
  }
  
  // TODO: This method requires a lot more consideration if we going to expand the user base in future.
  //  There may be situations when a file or test/function in the IDE isn't yet in git. We should decide
  //  what to do then. It may not be right to throw an error because if it's a parallel build, all the
  //  builds must run in order to produce a final result. It may not be right to run them as empty code
  //  tests because then it will show as passed confusing someone who is still developing.
  //  Since right now there aren't many people using the system, we will apply a solution that works for
  //  current customers.
  private void setCodeToTestVersions(List<TestVersion> testVersions,
                                    Map<String, Map<String, String>> testToCodeByFile) {
    for (TestVersion testVersion : testVersions) {
      String fileName = testVersion.getFile().getName();
      if (!testToCodeByFile.containsKey(fileName)) {
        // Looks like this file hasn't been pushed to git yet. Let's include it in the build anyway
        LOG.error("File {} doesn't exist in the repo.", fileName);
        resetCodeIfNotExistsInGit(testVersion);
        continue;
      }
      
      String testName = testVersion.getTest().getName();
      Map<String, String> testToCode = testToCodeByFile.get(fileName);
      if (!testToCode.containsKey(testName)) {
        // Looks like this test hasn't been pushed to git yet. Let's include it in the build anyway
        LOG.error("Test {} doesn't exist in file {} in the repo.",
            testName, fileName);
        resetCodeIfNotExistsInGit(testVersion);
        continue;
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
  public Optional<TestVersion> getFunctionAsTestVersion(int projectId,
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
    
    // If this function doesn't exist in git, let's do the same thing we do when it doesn't exist
    // in db. This will let user know that the function hasn't yet been pushed to git because it's
    // possible user may have pushed the test but not the associated function.
    if (!(testToCodeByFile.containsKey(fileName)
        && testToCodeByFile.get(fileName).containsKey(testName))) {
      return Optional.empty();
    }
  
    setCodeToTestVersions(Collections.singletonList(testVersion), testToCodeByFile);
    return optionalTestVersion;
  }
}
