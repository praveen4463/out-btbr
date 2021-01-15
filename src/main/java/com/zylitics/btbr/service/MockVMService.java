package com.zylitics.btbr.service;

import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockVMService implements VMService {
  
  private static final Logger LOG = LoggerFactory.getLogger(MockVMService.class);
  
  @Override
  public void setVMAsAvailable(BuildVM buildVM, Build build) {
    LOG.debug("Mock VM service getting set VM to available request: buildVM {}, buildId {}",
        buildVM, build.getBuildId());
  }
  
  @Override
  public void deleteVM(BuildVM buildVM) {
    LOG.debug("Mock VM service getting delete VM request: buildVM {}", buildVM);
  }
}
