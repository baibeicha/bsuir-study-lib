package com.github.baibeicha.database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<Target> {
    Target mapRow(ResultSet rs) throws SQLException;
}
