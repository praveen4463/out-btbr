package com.zylitics.btbr.runner;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app-short-version}")
public class StatusController {
  
  @GetMapping("/status")
  public ResponseEntity<Void> status() {
    return ResponseEntity.ok().build();
  }
}
