package org.forest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FireDao {
    private final Connection connection;

    public FireDao(Connection connection) {
        this.connection = connection;
    }

    public int getOrCreateStateId(String stateName) throws SQLException {
        int id = -1;
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM states WHERE name = ?")) {
            ps.setString(1, stateName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ins = connection.prepareStatement("INSERT INTO states(name) VALUES(?)")) {
            ins.setString(1, stateName);
            ins.executeUpdate();
        }
        try (PreparedStatement ps2 = connection.prepareStatement("SELECT id FROM states WHERE name = ?")) {
            ps2.setString(1, stateName);
            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
        }
        return id;
    }

    public void upsertFireCount(String stateName, int year, int count) throws SQLException {
        int stateId = getOrCreateStateId(stateName);
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO fires(state_id, year, count) VALUES(?,?,?) " +
                        "ON CONFLICT(state_id, year) DO UPDATE SET count = excluded.count")) {
            ps.setInt(1, stateId);
            ps.setInt(2, year);
            ps.setInt(3, count);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> topStatesByYear(int year, int limit) throws SQLException {
        String sql = "SELECT s.name, f.count FROM fires f JOIN states s ON s.id = f.state_id WHERE f.year = ? ORDER BY f.count DESC, s.name ASC LIMIT ?";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("state", rs.getString(1));
                    row.put("count", rs.getInt(2));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<Map<String, Object>> yoyForState(String stateName) throws SQLException {
        String sql = "SELECT f.year, f.count FROM fires f JOIN states s ON s.id = f.state_id WHERE s.name = ? ORDER BY f.year";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, stateName);
            try (ResultSet rs = ps.executeQuery()) {
                Integer prev = null;
                while (rs.next()) {
                    int year = rs.getInt(1);
                    int count = rs.getInt(2);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("year", year);
                    row.put("count", count);
                    if (prev != null) {
                        row.put("yoy", count - prev);
                    } else {
                        row.put("yoy", null);
                    }
                    rows.add(row);
                    prev = count;
                }
            }
        }
        return rows;
    }

    public Map<String, List<Map<String, Object>>> compareStates(List<String> states) throws SQLException {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (String s : states) {
            result.put(s, yoyForState(s));
        }
        return result;
    }

    public List<Map<String, Object>> summaryForYear(int year) throws SQLException {
        String sql = "SELECT s.name, COALESCE(f.count, 0) FROM states s LEFT JOIN fires f ON f.state_id = s.id AND f.year = ? ORDER BY s.name";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("state", rs.getString(1));
                    row.put("count", rs.getInt(2));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<String> statesWithZeroFires() throws SQLException {
        String sql = "SELECT s.name FROM states s WHERE NOT EXISTS (SELECT 1 FROM fires f WHERE f.state_id = s.id AND f.count > 0) ORDER BY s.name";
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        }
        return list;
    }

    public Map<String, Object> mostVulnerableRegion() throws SQLException {
        String sql = "SELECT s.name, SUM(f.count) AS total FROM fires f JOIN states s ON s.id = f.state_id GROUP BY s.id ORDER BY total DESC, s.name ASC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("state", rs.getString(1));
                row.put("total", rs.getInt(2));
                return row;
            }
        }
        return null;
    }
}


