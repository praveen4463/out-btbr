package com.zylitics.btbr.dao;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractDaoProvider {
  
  final NamedParameterJdbcTemplate jdbc;

  AbstractDaoProvider(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
}
