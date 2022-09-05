package com.zylitics.btbr.service;

import com.google.common.base.Charsets;
import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.api.ZwlApi;
import com.zylitics.zwl.model.ZwlFileTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ZWLFileReader {
  
  private final Map<String, Map<String, String>> testToCodeByFile = new HashMap<>();
  
  private final StoringErrorListener storingErrorListener = new StoringErrorListener();
  
  private final String zwlProjectDir;
  
  public ZWLFileReader(String zwlProjectDir) {
    this.zwlProjectDir = zwlProjectDir;
  }
  
  public Map<String, Map<String, String>> readFiles(Set<String> files) throws IOException {
    for (String file: files) {
      if (testToCodeByFile.containsKey(file)) {
        continue;
      }
      Path zwlFilePath = Paths.get(zwlProjectDir, file + ".zwl");
      if (!Files.exists(zwlFilePath)) {
        continue; // Just continue and let caller decide what to do in that case.
      }
      
      List<ZwlFileTest> tests = new ArrayList<>();
      ZwlApi zwlApi = new ZwlApi(zwlFilePath, Charsets.UTF_8,
          Collections.singletonList(storingErrorListener));
      zwlApi.interpret(tests);
      Map<String, String> testToCode = new HashMap<>();
      tests.forEach(zwlFileTest ->
          testToCode.put(zwlFileTest.getTestName(), zwlFileTest.getCode()));
      testToCodeByFile.put(file, testToCode);
    }
    return testToCodeByFile;
  }
  
  public Map<String, Map<String, String>> readFile(String file) throws IOException {
    Set<String> files = new HashSet<>();
    files.add(file);
    return readFiles(files);
  }
}
