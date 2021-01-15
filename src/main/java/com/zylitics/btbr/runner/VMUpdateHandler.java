package com.zylitics.btbr.runner;

import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildVM;
import com.zylitics.btbr.runner.provider.BuildVMProvider;
import com.zylitics.btbr.runner.provider.BuildVMUpdateDeleteDate;
import com.zylitics.btbr.service.VMService;
import com.zylitics.btbr.util.DateTimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class VMUpdateHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(VMUpdateHandler.class);
  
  private final VMService vmService;
  private final BuildVMProvider buildVMProvider;
  
  VMUpdateHandler(VMService vmService,
                  BuildVMProvider buildVMProvider) {
    this.vmService = vmService;
    this.buildVMProvider = buildVMProvider;
  }
  
  // log any exception and don't throw.
  void update(Build build) {
    int buildVMId = build.getBuildVMId();
    try {
      BuildVM buildVM = buildVMProvider.getBuildVM(buildVMId)
          .orElseThrow(() -> new RuntimeException("Couldn't get buildVM for " + buildVMId));
      if (!buildVM.isDeleteFromRunner()) {
        vmService.setVMAsAvailable(buildVM, build);
        return;
      }
      LOG.debug("updating vm deleteDate for buildVMId {}", buildVMId);
      int result = buildVMProvider.updateDeleteDate(new BuildVMUpdateDeleteDate(buildVMId,
          DateTimeUtil.getCurrentUTC()));
      if (result != 1) {
        LOG.error("VM deleteDate couldn't be updated for buildVMId {}", buildVMId);
      }
      LOG.debug("going to delete vm for buildVMId {}", buildVMId);
      vmService.deleteVM(buildVM);
    } catch (Throwable t) {
      LOG.error("Failing to update vm for buildVMId " + buildVMId, t);
    }
  }
}
