package org.forest.app;

import org.forest.dao.FireDao;
import org.forest.db.Database;
import org.forest.db.Migrations;
import org.forest.importer.CsvImporter;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Migrations.run(conn);
            FireDao dao = new FireDao(conn);
            CsvImporter importer = new CsvImporter(dao);
            java.nio.file.Path csv = Paths.get("datafile.csv");
            if (csv.toFile().exists()) {
                importer.importFile(csv);
            }
            cli(dao);
        }
    }

    private static void cli(FireDao dao) throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("Forest Fires - Menu");
            System.out.println("1) Insert/Update record");
            System.out.println("2) Top 5 states by year");
            System.out.println("3) YoY growth/decline for a state");
            System.out.println("4) Compare multiple states");
            System.out.println("5) Summary table for a year");
            System.out.println("6) States with 0 fires");
            System.out.println("7) Most vulnerable region");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("State: ");
                    String s = sc.nextLine();
                    System.out.print("Year: ");
                    int y = Integer.parseInt(sc.nextLine());
                    System.out.print("Count: ");
                    int c = Integer.parseInt(sc.nextLine());
                    dao.upsertFireCount(s, y, c);
                    System.out.println("Saved.");
                    break;
                case "2":
                    System.out.print("Year: ");
                    int year = Integer.parseInt(sc.nextLine());
                    dao.topStatesByYear(year, 5).forEach(r ->
                            System.out.println(r.get("state") + ": " + r.get("count"))
                    );
                    break;
                case "3":
                    System.out.print("State: ");
                    String st = sc.nextLine();
                    dao.yoyForState(st).forEach(r ->
                            System.out.println(r.get("year") + ": " + r.get("count") + " (YoY: " + r.get("yoy") + ")")
                    );
                    break;
                case "4":
                    System.out.print("States (comma separated): ");
                    String line = sc.nextLine();
                    List<String> states = new ArrayList<>();
                    for (String part : line.split(",")) {
                        String t = part.trim();
                        if (!t.isEmpty()) states.add(t);
                    }
                    Map<String, List<Map<String, Object>>> cmp = dao.compareStates(states);
                    for (String k : cmp.keySet()) {
                        System.out.println("== " + k + " ==");
                        for (Map<String, Object> r : cmp.get(k)) {
                            System.out.println(r.get("year") + ": " + r.get("count") + " (YoY: " + r.get("yoy") + ")");
                        }
                    }
                    break;
                case "5":
                    System.out.print("Year: ");
                    int yr = Integer.parseInt(sc.nextLine());
                    System.out.println("State | Count");
                    dao.summaryForYear(yr).forEach(r ->
                            System.out.println(r.get("state") + " | " + r.get("count"))
                    );
                    break;
                case "6":
                    dao.statesWithZeroFires().forEach(System.out::println);
                    break;
                case "7":
                    Map<String, Object> mv = dao.mostVulnerableRegion();
                    if (mv != null) {
                        System.out.println(mv.get("state") + " total: " + mv.get("total"));
                    } else {
                        System.out.println("No data.");
                    }
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}


