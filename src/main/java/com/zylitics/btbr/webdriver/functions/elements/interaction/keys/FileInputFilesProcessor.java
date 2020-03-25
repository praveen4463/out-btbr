package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.zwl.exception.ZwlLangException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Downloads the given files using GCP {@link Storage}, saves them locally in a temporary location
 * , and returns the paths of saved files.
 * Note that the deletion of directories on VM exist can be done at root which is the build dir,
 * thus individual dir like download dir don't require a shutdown hook.
 */
class FileInputFilesProcessor {
  
  private static final String USER_DOWNLOAD_DIR = "user_downloads";
  
  private final Storage storage;
  
  private final String userAccountBucket;
  
  private final String pathToUploadedFiles;
  
  private final Set<String> fileNames;
  
  private final Path buildDir; // dir where all files for current build should be stored.
  
  private final Supplier<String> lineNColumn;
  
  FileInputFilesProcessor(Storage storage,
                          String userAccountBucket,
                          String pathToUploadedFiles,
                          Set<String> fileNames,
                          Path buildDir,
                          Supplier<String> lineNColumn) {
    Preconditions.checkNotNull(storage, "storage can't be null");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccountBucket),
        "userAccountBucket can't be empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(pathToUploadedFiles),
        "pathToUploadedFiles can't be empty");
    Preconditions.checkArgument(fileNames.size() == 0, "fileNames can't be empty");
    Preconditions.checkNotNull(buildDir, "buildDir can't be null");
    
    this.storage = storage;
    this.userAccountBucket = userAccountBucket;
    this.pathToUploadedFiles = pathToUploadedFiles;
    this.fileNames = fileNames;
    this.buildDir = buildDir;
    this.lineNColumn = lineNColumn;
  }
  
  /**
   *
   * @return list of local paths for the provided files.
   * @throws ZwlLangException when a {@link RuntimeException} is occurred so that user is
   * notified about the problem
   */
  Set<String> process() throws ZwlLangException {
    if (!Files.isDirectory(buildDir)) {
      throw new RuntimeException(buildDir.toAbsolutePath().toString() + " isn't a directory");
    }
    
    Path userDownloads = buildDir.resolve(USER_DOWNLOAD_DIR);
    createNotExistDir(userDownloads);
    Set<String> localPaths = new HashSet<>(CollectionUtil.getInitialCapacity(fileNames.size()));
    
    for (String fileName : fileNames) {
      Path filePath = userDownloads.resolve(fileName);
      // before proceeding for download, check whether the same file was downloaded in this
      // build, if so, skip download and use that. Even if user had a same named file in different
      // directories at storage, the fileName carries the full path to file because storage is a
      // flat filesystem, so if user put in root of their directory ship.pdf, fileName = ship.pdf,
      // if put in talk/biz/ship.pdf, fileName = talk/biz/ship.pdf, thus we can be sure same file
      // name refers to the same file (assuming content doesn't change within a build)
      if (Files.exists(filePath)) {
        localPaths.add(filePath.toAbsolutePath().toString());
        continue;
      }
      
      WritableByteChannel channel = null;
      try {
        Blob blob = storage.get(BlobId.of(userAccountBucket, constructFilePath(fileName)));
        if (blob == null) {
          throw new ZwlLangException(fileName + " doesn't exists. Please check whether this file" +
              " was really uploaded. " + lineNColumn.get());
        }
        // currently there is no support for user to put files in a directory structure at cloud
        // storage and they need to put all of them at one place flat. This makes it easier to not
        // deal with creating another directory path here before creating the file.
        if (!filePath.getParent().equals(userDownloads)) {
          throw new RuntimeException("Directory structure in user specified file isn't currently" +
              " supported.");
        }
        OutputStream stream = createFileAndOpenStream(filePath);
        
        if (blob.getSize() < 1_000_000) {
          stream.write(blob.getContent());
        } else {
          try (ReadChannel reader = blob.reader()) {
            channel = Channels.newChannel(stream);
            ByteBuffer bytes = ByteBuffer.allocate(64 * 1024); // 64 kilo bytes buffer
            while (reader.read(bytes) > 0) {
              bytes.flip();
              channel.write(bytes); // write into file whatever bytes have been read from channel
              bytes.clear();
            }
          }
        }
      } catch (Exception io) {
        // We should be catching only storage related exceptions here, IO error should be handled
        // in a separate try-catch if desired.
        // for now no reattempt or catching StorageException separately, just log and see what
        // errors we get.
        // TODO: watch exceptions and decide on reattempts and what to notify user
        throw new RuntimeException(io); // don't force caller handle an exception.
      } finally {
        if (channel != null) {
          try {
            channel.close(); // closes the associated stream.
          } catch (IOException ignore) {}
        }
      }
      localPaths.add(filePath.toAbsolutePath().toString());
    }
    return localPaths;
  }
  
  private void createNotExistDir(Path dir) {
    try {
      if (!Files.isDirectory(dir)) {
        Files.createDirectory(dir);
      }
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
  }
  
  private OutputStream createFileAndOpenStream(Path file) {
    try {
      Files.createFile(file);
      return Files.newOutputStream(file);
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
  }
  
  private String constructFilePath(String fileName) {
    return pathToUploadedFiles + (pathToUploadedFiles.endsWith("/") ? "" : "/") + fileName;
  }
}
