package com.zylitics.btbr;

import java.io.Closeable;

public interface SecretsManager extends Closeable {
  
  String getSecretAsPlainText(String secretCloudFileName);
}
