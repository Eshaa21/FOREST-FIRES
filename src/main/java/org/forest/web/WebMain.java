package org.forest.web;

import org.forest.dao.FireDao;
import org.forest.db.Database;
import org.forest.db.Migrations;
import org.forest.importer.CsvImporter;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class WebMain {
    public static void main(String[] args) throws Exception {
        final Connection conn = Database.getConnection();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException ignored) {}
        }));

        Migrations.run(conn);
        FireDao dao = new FireDao(conn);
        java.nio.file.Path csv = Paths.get("datafile.csv");
        if (csv.toFile().exists()) {
            new CsvImporter(dao).importFile(csv);
        }
        new WebServer(dao).start();
        System.out.println("Web server running at http://localhost:8080");
    }
}


