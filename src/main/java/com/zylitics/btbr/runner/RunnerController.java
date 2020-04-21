package com.zylitics.btbr.runner;

import com.google.cloud.storage.Storage;
import com.zylitics.btbr.config.APICoreProperties;
import com.zylitics.btbr.http.RequestBuildRun;
import com.zylitics.btbr.http.ResponseBuildRun;
import com.zylitics.btbr.runner.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app-short-version}/builds")
public class RunnerController {
  
  private static final Logger LOG = LoggerFactory.getLogger(RunnerController.class);
  
  private final APICoreProperties apiCoreProperties;
  private final Storage storage;
  private final CaptureShotHandler.Factory captureShotHandlerFactory;
  // DAO
  private final BuildProvider buildProvider;
  private final BuildStatusProvider buildStatusProvider;
  private final BuildVMProvider buildVMProvider;
  private final ImmutableMapProvider immutableMapProvider;
  private final ShotMetadataProvider shotMetadataProvider;
  private final TestVersionProvider testVersionProvider;
  private final ZwlProgramOutputProvider zwlProgramOutputProvider;
  
  @Autowired
  public RunnerController(APICoreProperties apiCoreProperties,
                          Storage storage,
                          CaptureShotHandler.Factory captureShotHandlerFactory,
                          BuildProvider buildProvider,
                          BuildStatusProvider buildStatusProvider,
                          BuildVMProvider buildVMProvider,
                          ImmutableMapProvider immutableMapProvider,
                          ShotMetadataProvider shotMetadataProvider,
                          TestVersionProvider testVersionProvider,
                          ZwlProgramOutputProvider zwlProgramOutputProvider) {
    this.apiCoreProperties = apiCoreProperties;
    this.storage = storage;
    this.captureShotHandlerFactory = captureShotHandlerFactory;
    this.buildProvider = buildProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.buildVMProvider = buildVMProvider;
    this.immutableMapProvider = immutableMapProvider;
    this.shotMetadataProvider = shotMetadataProvider;
    this.testVersionProvider = testVersionProvider;
    this.zwlProgramOutputProvider = zwlProgramOutputProvider;
  }
  
  @PostMapping
  public ResponseEntity<ResponseBuildRun> run(
      @Validated @RequestBody RequestBuildRun requestBuildRun) throws Exception {
    LOG.info("received request: {}", requestBuildRun.toString());
    
    // get build
    
  }
}
