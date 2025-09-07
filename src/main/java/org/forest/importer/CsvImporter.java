package org.forest.importer;

import com.opencsv.CSVReader;
import org.forest.dao.FireDao;

import java.io.FileReader;
import java.nio.file.Path;
import java.sql.SQLException;

public class CsvImporter {
    private final FireDao fireDao;

    public CsvImporter(FireDao fireDao) {
        this.fireDao = fireDao;
    }

    public void importFile(Path csvPath) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(csvPath.toFile()))) {
            String[] header = reader.readNext();
            if (header == null || header.length < 2) return;
            int[] years = new int[header.length - 1];
            for (int i = 1; i < header.length; i++) {
                years[i - 1] = parseYear(header[i]);
            }
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length == 0) continue;
                String state = sanitizeState(row[0]);
                for (int i = 1; i < row.length && i - 1 < years.length; i++) {
                    int year = years[i - 1];
                    Integer count = parseInt(row[i]);
                    if (count != null) {
                        tryUpsert(state, year, count);
                    }
                }
            }
        }
    }

    private void tryUpsert(String state, int year, int count) throws SQLException {
        fireDao.upsertFireCount(state, year, count);
    }

    private static Integer parseInt(String s) {
        try {
            if (s == null) return null;
            s = s.trim().replaceAll("[^0-9-]", "");
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseYear(String label) {
        // Handles formats like "2010-2011" or "2009-10"
        String t = label.trim();
        if (t.matches("\\d{4}-\\d{4}")) {
            return Integer.parseInt(t.substring(0, 4));
        }
        if (t.matches("\\d{4}-\\d{2}")) {
            return Integer.parseInt(t.substring(0, 4));
        }
        // Fallback: extract first 4-digit number
        String digits = t.replaceAll("[^0-9]", "");
        if (digits.length() >= 4) {
            return Integer.parseInt(digits.substring(0, 4));
        }
        return 0;
    }

    private static String sanitizeState(String name) {
        return name == null ? null : name.trim();
    }
}


