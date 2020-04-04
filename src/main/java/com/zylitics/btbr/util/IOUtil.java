package com.zylitics.btbr.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class IOUtil {
  
  /**
   * Creates a file of specified name in the given dir and writes the given bytes into it.
   * If the file already exists in dir, appends some random identifier to the file name and tries
   * saving again.
   */
  // TODO: work on this while working on saving logs, as this will go into the same location where
  // logs are saved and will then go into cloud storage on build completion at some accessible
  // location for user.
  public static void write(byte[] data, String file, String dir) throws IOException {
  
  }
  
  public static void createNonExistingDir(Path dir) throws IOException {
    if (!Files.isDirectory(dir)) {
      Files.createDirectory(dir);
    }
  }
  
  /**
   * Deletes a directory after deleting it's children recursively. (subdirectories and files).
   * @param dir The root dir to delete
   * @throws IOException when any error occurs during the recursive operation.
   */
  public static void deleteDir(Path dir) throws IOException {
    Files.walkFileTree(dir, new FileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
      }
  
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }
  
      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        throw exc;
      }
  
      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) {
          throw exc;
        }
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
