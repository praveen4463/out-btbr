package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.BuildCapability;

import java.util.Optional;

public interface BuildCapabilityProvider {
  
  Optional<BuildCapability> getBuildCapability(int buildCapabilityId);
}
