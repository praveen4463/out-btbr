package com.zylitics.btbr.webdriver.functions;

import com.google.common.collect.ImmutableMap;
import com.zylitics.btbr.webdriver.functions.action.PerformAction;
import com.zylitics.zwl.datatype.ListZwlValue;
import com.zylitics.zwl.datatype.MapZwlValue;
import com.zylitics.zwl.datatype.ZwlValue;
import com.zylitics.zwl.function.AbstractFunction;
import java.util.List;
import java.util.function.Supplier;

/**
 * An abstract class for functions that need to return only their signature and parameter list,
 * cause they are meant to be processed by the function (rather than processed by ZWL's interpreter)
 * that takes them as argument. An example is function
 * {@link PerformAction}, that take sequence of
 * actions as arguments, those actions are also functions but interpreter can't process them
 * because they work in context of an action which requires a state to be managed where those
 * functions add/update interactions and once all interactions are added, should be submitted
 * together to webdriver. Thus we tell {@link PerformAction} what are the actions user requires
 * by only giving the name of action and arguments given.
 */
public abstract class AbstractSignatureReturningFunctions extends AbstractFunction {
  
  // Most functions may not take any argument.
  @Override
  public int minParamsCount() {
    return 0;
  }
  
  @Override
  public int maxParamsCount() {
    return 0;
  }
  
  @Override
  public ZwlValue invoke(List<ZwlValue> args, Supplier<ZwlValue> defaultValue,
                         Supplier<String> lineNColumn) {
    super.invoke(args, defaultValue, lineNColumn);
    
    // no command update will be sent by these functions.
    return new MapZwlValue(ImmutableMap.of(getName(), new ListZwlValue(args)));
  }
}
