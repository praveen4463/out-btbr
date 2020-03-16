package com.zylitics.btbr.webdriver.functions.elements.interaction.keys;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.zwl.exception.ZwlLangException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Downloads the given files using GCP {@link Storage}, saves them locally in a temporary location
 * , and returns the paths of saved files.
 */
class FileInputFilesProcessor {
  
  private static final Logger LOG = LoggerFactory.getLogger(FileInputFilesProcessor.class);
  
  private final Storage storage;
  
  private final String userAccountBucket;
  
  private final String pathToUploadedFiles;
  
  private final Set<String> fileNames;
  
  FileInputFilesProcessor(Storage storage,
                          String userAccountBucket,
                          String pathToUploadedFiles,
                          Set<String> fileNames) {
    Preconditions.checkNotNull(storage, "storage can't be null");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccountBucket),
        "userAccountBucket can't be empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(pathToUploadedFiles),
        "pathToUploadedFiles can't be empty");
    Preconditions.checkArgument(fileNames.size() == 0, "fileNames can't be empty");
    
    
    this.storage = storage;
    this.userAccountBucket = userAccountBucket;
    this.pathToUploadedFiles = pathToUploadedFiles;
    this.fileNames = fileNames;
  }
  
  /**
   *
   * @return list of local paths for the provided files.
   * @throws ZwlLangException when a {@link RuntimeException} is occurred so that user is
   * notified about the problem
   */
  Set<String> process() throws ZwlLangException {
    Set<String> localPaths = new HashSet<>(CollectionUtil.getInitialCapacity(fileNames.size()));
    
    for (String fileName : fileNames) {
      WritableByteChannel channel = null;
      Path file;
      try {
        Blob blob = storage.get(BlobId.of(userAccountBucket, constructFilePath(fileName)));
        if (blob == null) {
          throw new ZwlLangException(fileName + " doesn't exists. Please check whether this file" +
              " was really uploaded.");
        }
        
        // create separate directory under temp dir for each file so that even if file names are
        // repeated across function calls, they all get into local file system without overwriting
        // other and carry unique path.
        Path dir = Files.createTempDirectory(UUID.randomUUID().toString());
        file = Files.createFile(Paths.get(dir.toString(), fileName));
        OutputStream stream = Files.newOutputStream(file);
        
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
        // for now no reattempt or catching StorageException separately, just log and see what
        // errors we get.
        // TODO: watch exceptions and decide on reattempts and what to notify user
        LOG.error(io.getMessage(), io);
        throw new RuntimeException(io); // don't force caller handle an exception.
      } finally {
        if (channel != null) {
          try {
            channel.close(); // closes the associated stream.
          } catch (IOException ignore) {}
        }
      }
      localPaths.add(file.toAbsolutePath().toString());
    }
    return localPaths;
  }
  
  private String constructFilePath(String fileName) {
    return pathToUploadedFiles + (pathToUploadedFiles.endsWith("/") ? "" : "/") + fileName;
  }
}
