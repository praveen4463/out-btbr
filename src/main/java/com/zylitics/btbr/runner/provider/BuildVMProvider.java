package com.zylitics.btbr.runner.provider;

import com.zylitics.btbr.model.BuildVM;

import java.util.Optional;

public interface BuildVMProvider {
  
  Optional<BuildVM> getBuildVM(int buildId);
  
  int updateDeleteDate(BuildVMUpdateDeleteDate buildVMUpdateDeleteDate);
}
