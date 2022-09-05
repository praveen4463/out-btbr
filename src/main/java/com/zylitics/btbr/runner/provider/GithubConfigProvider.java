package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.GithubConfig;

import java.util.Optional;

public interface GithubConfigProvider {
  
  Optional<GithubConfig> getGithubConfig(int organizationId);
}
