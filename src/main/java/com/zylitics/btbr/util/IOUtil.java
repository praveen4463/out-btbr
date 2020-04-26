package com.zylitics.btbr.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class IOUtil {
  
  /**
   * Creates dir if it doesn't exists.
   * @param dir {@link Path} of the dir to create
   * @throws RuntimeException Rather than an {@link IOException}, a RuntimeException is thrown so
   * that caller doesn't have to handle it.
   */
  public static void createNonExistingDir(Path dir) throws RuntimeException {
    if (!Files.isDirectory(dir)) {
      try {
        Files.createDirectory(dir);
      } catch (IOException io) {
        throw new RuntimeException("Couldn't create directory at " + dir.toAbsolutePath(), io);
      }
    }
  }
  
  /**
   * Creates dir.
   * @param dir {@link Path} of the dir to create
   * @throws RuntimeException Rather than an {@link IOException}, a RuntimeException is thrown so
   * that caller doesn't have to handle it.
   */
  public static void createDir(Path dir) throws RuntimeException {
    try {
      Files.createDirectory(dir);
    } catch (IOException io) {
      throw new RuntimeException("Couldn't create directory at " + dir.toAbsolutePath(), io);
    }
  }
  
  /**
   * Deletes a directory after deleting it's children recursively. (subdirectories and files).
   * @param dir The root dir to delete
   * @throws IOException when any error occurs during the recursive operation.
   */
  public static void deleteDir(Path dir) throws IOException {
    if (!Files.isDirectory(dir)) {
      return;
    }
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
