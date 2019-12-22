package com.zylitics.btbr;

import java.io.Closeable;

interface SecretsManager extends Closeable {
  
  String getSecretAsPlainText(String secretCloudFileName);
}
