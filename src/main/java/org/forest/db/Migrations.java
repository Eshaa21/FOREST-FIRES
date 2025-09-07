package org.forest.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Migrations {
    public static void run(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS states (\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  name TEXT NOT NULL UNIQUE\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS fires (\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  state_id INTEGER NOT NULL,\n" +
                    "  year INTEGER NOT NULL,\n" +
                    "  count INTEGER NOT NULL,\n" +
                    "  UNIQUE(state_id, year),\n" +
                    "  FOREIGN KEY(state_id) REFERENCES states(id) ON DELETE CASCADE\n" +
                    ")");

            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fires_year ON fires(year)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fires_state ON fires(state_id)");
        }
    }
}


