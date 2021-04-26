package com.zylitics.btbr.service;

import com.zylitics.btbr.model.Build;
import com.zylitics.btbr.model.BuildVM;

public interface VMService {
  
  void setVMAsAvailable(BuildVM buildVM);
  
  void deleteVM(BuildVM buildVM);
}