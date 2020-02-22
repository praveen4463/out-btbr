package com.zylitics.btbr.runner.provider;

import java.util.Map;
import java.util.Optional;

/**
 * Provider for tables that contains immutable maps such as zwl_globals, zwl_preferences.
 */
public interface ImmutableMapProvider {
  
  Optional<Map<String, String>> getMapFromTable(String table);
}
