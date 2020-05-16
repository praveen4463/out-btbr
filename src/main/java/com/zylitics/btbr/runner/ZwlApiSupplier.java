package com.zylitics.btbr.runner;

import com.zylitics.zwl.api.ZwlApi;
import org.antlr.v4.runtime.ANTLRErrorListener;

import java.util.List;

/** For better testing of {@link ZwlApi} */
class ZwlApiSupplier {
  
  ZwlApi get(String code, List<ANTLRErrorListener> errorListeners) {
    return new ZwlApi(code, errorListeners);
  }
}
