package org.forest.web;

import com.google.gson.Gson;
import org.forest.dao.FireDao;

import java.util.*;

import static spark.Spark.*;

public class WebServer {
    private final FireDao dao;
    private final Gson gson = new Gson();

    public WebServer(FireDao dao) {
        this.dao = dao;
    }

    public void start() {
        String p = System.getenv("PORT");
        port(p != null ? Integer.parseInt(p) : 8080);        staticFiles.location("/public");

        after((req, res) -> {
            String path = req.pathInfo();
            if (path != null && path.startsWith("/api")) {
                res.header("Content-Type", "application/json");
            }
        });

        post("/api/upsert", (req, res) -> {
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            String state = String.valueOf(body.get("state"));
            int year = ((Number) body.get("year")).intValue();
            int count = ((Number) body.get("count")).intValue();
            dao.upsertFireCount(state, year, count);
            java.util.Map<String,Object> ok = new java.util.LinkedHashMap<>();
            ok.put("ok", true);
            return gson.toJson(ok);
        });

        get("/api/top5", (req, res) -> {
            int year = Integer.parseInt(req.queryParams("year"));
            return gson.toJson(dao.topStatesByYear(year, 5));
        });

        get("/api/yoy", (req, res) -> {
            String state = req.queryParams("state");
            return gson.toJson(dao.yoyForState(state));
        });

        get("/api/compare", (req, res) -> {
            String statesParam = req.queryParams("states");
            List<String> states = new ArrayList<>();
            if (statesParam != null) {
                for (String s : statesParam.split(",")) {
                    String t = s.trim();
                    if (!t.isEmpty()) states.add(t);
                }
            }
            return gson.toJson(dao.compareStates(states));
        });

        get("/api/summary", (req, res) -> {
            int year = Integer.parseInt(req.queryParams("year"));
            return gson.toJson(dao.summaryForYear(year));
        });

        get("/api/zeros", (req, res) -> gson.toJson(dao.statesWithZeroFires()));

        get("/api/vulnerable", (req, res) -> gson.toJson(dao.mostVulnerableRegion()));
    }
}


