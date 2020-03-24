package com.zylitics.btbr.webdriver.functions.until;

import com.zylitics.btbr.util.CollectionUtil;
import com.zylitics.zwl.datatype.DoubleZwlValue;
import com.zylitics.zwl.datatype.MapZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.function.AbstractFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class UntilFlt extends AbstractFunction {
  
  @Override
  public String getName() {
    return "untilFlt";
  }
  
  @Override
  public int minParamsCount() {
    return 1;
  }
  
  @Override
  public int maxParamsCount() {
    return 2;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
  
    int argsCount = args.size();
    if (argsCount == 0) {
      throw unexpectedEndOfFunctionOverload(argsCount);
    }
    Double timeout = parseDouble(0, args.get(0));
    Double poll = 0d;
    if (argsCount == 2) {
      poll = parseDouble(1, args.get(1));
    }
    Map<String, ZwlValue> m = new HashMap<>(CollectionUtil.getInitialCapacity(2));
    m.put(AbstractUntilExpectation.TIMEOUT_KEY, new DoubleZwlValue(timeout));
    m.put(AbstractUntilExpectation.POLL_KEY, new DoubleZwlValue(poll));
    return new MapZwlValue(m);
  }
}
