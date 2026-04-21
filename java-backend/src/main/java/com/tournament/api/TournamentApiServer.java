package com.tournament.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tournament.factory.SportFactory;

import java.sql.*;
import java.util.*;

import static spark.Spark.*;

/**
 * REST API Server — exposes endpoints consumed by the Streamlit frontend.
 * Uses Spark Java (lightweight HTTP framework) + JDBC for MySQL.
 *
 * Start with: mvn exec:java -Dexec.mainClass="com.tournament.api.TournamentApiServer"
 *
 * PORT: 4567  (default Spark port)
 */
public class TournamentApiServer {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/tournament_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";  // ← change in production

    private static final Gson gson = new Gson();

    // ─────────────────────────────────────────
    // DB helper
    // ─────────────────────────────────────────
    private static Connection getConn() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private static String ok(Object data) {
        JsonObject r = new JsonObject();
        r.addProperty("status", "success");
        r.add("data", gson.toJsonTree(data));
        return gson.toJson(r);
    }

    private static String err(String msg) {
        JsonObject r = new JsonObject();
        r.addProperty("status", "error");
        r.addProperty("message", msg);
        return gson.toJson(r);
    }

    // ─────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────
    public static void main(String[] args) {
        port(4567);

        // CORS
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin",  "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
            res.type("application/json");
        });
        options("/*", (req, res) -> "OK");

        // ── Health ──────────────────────────────
        get("/health", (req, res) ->
            "{\"status\":\"UP\",\"service\":\"Tournament API\"}");

        // ────────────────────────────────────────
        // TOURNAMENTS
        // ────────────────────────────────────────
       get("/tournaments", (req, res) -> {
    List<Map<String,Object>> list = new ArrayList<>();

    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(
             "SELECT t.*, l.venue_name, l.city, u.name AS creator_name " +
             "FROM tournaments t " +
             "LEFT JOIN locations l ON l.location_id = t.location_id " +
             "LEFT JOIN users u ON u.user_id = t.created_by " +
             "ORDER BY t.created_at DESC")) {

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("tournament_id",  rs.getInt("tournament_id"));
            row.put("name",           rs.getString("name"));
            row.put("sport_type",     rs.getString("sport_type"));
            row.put("start_date",     rs.getString("start_date"));
            row.put("end_date",       rs.getString("end_date"));
            row.put("status",         rs.getString("status"));
            row.put("venue_name",     rs.getString("venue_name"));
            row.put("city",           rs.getString("city"));
            list.add(row);
        }

        return ok(list);

    } catch (Exception e) {
        e.printStackTrace();   // 🔥 shows real error in terminal
        res.status(500);
        return err("Database error: " + e.getMessage());
    }
});

        post("/tournaments", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            try {
                // Validate sport via factory
                SportFactory.createSport(body.get("sport_type").getAsString());

                try (Connection c = getConn();
                     PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO tournaments (name, sport_type, start_date, end_date, location_id, created_by) " +
                         "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, body.get("name").getAsString());
                    ps.setString(2, body.get("sport_type").getAsString());
                    ps.setString(3, body.get("start_date").getAsString());
                    ps.setString(4, body.get("end_date").getAsString());
                    ps.setInt   (5, body.get("location_id").getAsInt());
                    ps.setInt   (6, body.get("created_by").getAsInt());
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    keys.next();
                    return ok(Map.of("tournament_id", keys.getInt(1), "message", "Tournament created"));
                }
            } catch (Exception e) {
                res.status(400);
                return err(e.getMessage());
            }
        });

        // ────────────────────────────────────────
        // TEAMS
        // ────────────────────────────────────────
        get("/teams", (req, res) -> {
            List<Map<String,Object>> list = new ArrayList<>();
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT t.*, u.name AS manager_name, COUNT(p.player_id) AS player_count " +
                     "FROM teams t " +
                     "JOIN users u ON u.user_id = t.manager_id " +
                     "LEFT JOIN players p ON p.team_id = t.team_id " +
                     "GROUP BY t.team_id ORDER BY t.team_id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("team_id",      rs.getInt("team_id"));
                    row.put("team_name",    rs.getString("team_name"));
                    row.put("manager_name", rs.getString("manager_name"));
                    row.put("player_count", rs.getInt("player_count"));
                    list.add(row);
                }
            }
            return ok(list);
        });

        post("/teams", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO teams (team_name, manager_id) VALUES (?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, body.get("team_name").getAsString());
                ps.setInt   (2, body.get("manager_id").getAsInt());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                return ok(Map.of("team_id", keys.getInt(1), "message", "Team created"));
            } catch (Exception e) {
                res.status(400);
                return err(e.getMessage());
            }
        });

        // ────────────────────────────────────────
        // PLAYERS
        // ────────────────────────────────────────
        get("/teams/:teamId/players", (req, res) -> {
            List<Map<String,Object>> list = new ArrayList<>();
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM players WHERE team_id=? ORDER BY player_id")) {
                ps.setInt(1, Integer.parseInt(req.params("teamId")));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("player_id", rs.getInt("player_id"));
                    row.put("name",      rs.getString("name"));
                    row.put("age",       rs.getInt("age"));
                    row.put("skill",     rs.getString("skill"));
                    list.add(row);
                }
            }
            return ok(list);
        });

        post("/players", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO players (name, age, skill, team_id) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, body.get("name").getAsString());
                ps.setInt   (2, body.get("age").getAsInt());
                ps.setString(3, body.get("skill").getAsString());
                ps.setInt   (4, body.get("team_id").getAsInt());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                return ok(Map.of("player_id", keys.getInt(1), "message", "Player added"));
            } catch (Exception e) {
                res.status(400);
                return err(e.getMessage());
            }
        });

        // ────────────────────────────────────────
        // REGISTRATIONS  (triggers payment)
        // ────────────────────────────────────────
        post("/registrations", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            try (Connection c = getConn()) {
                // Insert registration
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO registrations (tournament_id, team_id) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, body.get("tournament_id").getAsInt());
                ps.setInt(2, body.get("team_id").getAsInt());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                int regId = keys.getInt(1);

                // Trigger Payment Gateway (Command Pattern)
                double amount = body.has("amount") ? body.get("amount").getAsDouble() : 500.00;
                String gatewayRef = "GW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                PreparedStatement psPay = c.prepareStatement(
                    "INSERT INTO payments (registration_id, amount, status, gateway_ref, paid_at) " +
                    "VALUES (?,?,'Completed',?,NOW())");
                psPay.setInt   (1, regId);
                psPay.setDouble(2, amount);
                psPay.setString(3, gatewayRef);
                psPay.executeUpdate();

                // Confirm registration
                PreparedStatement psUpd = c.prepareStatement(
                    "UPDATE registrations SET status='Confirmed' WHERE registration_id=?");
                psUpd.setInt(1, regId);
                psUpd.executeUpdate();

                return ok(Map.of("registration_id", regId,
                                 "payment_ref", gatewayRef,
                                 "message", "Team registered & payment processed"));
            } catch (Exception e) {
                res.status(400);
                return err(e.getMessage());
            }
        });

        get("/registrations/:tournamentId", (req, res) -> {
            List<Map<String,Object>> list = new ArrayList<>();
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT r.*, t.team_name FROM registrations r " +
                     "JOIN teams t ON t.team_id = r.team_id " +
                     "WHERE r.tournament_id=?")) {
                ps.setInt(1, Integer.parseInt(req.params("tournamentId")));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("registration_id", rs.getInt("registration_id"));
                    row.put("team_id",   rs.getInt("team_id"));
                    row.put("team_name", rs.getString("team_name"));
                    row.put("status",    rs.getString("status"));
                    list.add(row);
                }
            }
            return ok(list);
        });

        // ────────────────────────────────────────
        // MATCHES
        // ────────────────────────────────────────
        get("/matches", (req, res) -> {
            List<Map<String,Object>> list = new ArrayList<>();
            String tournamentFilter = req.queryParams("tournament_id");
            String sql = "SELECT m.*, t1.team_name AS team1_name, t2.team_name AS team2_name, " +
                         "u.name AS referee_name, l.venue_name, l.city, " +
                         "s.team1_score, s.team2_score, s.winner_team_id, " +
                         "wt.team_name AS winner_name " +
                         "FROM matches m " +
                         "JOIN teams t1 ON t1.team_id = m.team1_id " +
                         "JOIN teams t2 ON t2.team_id = m.team2_id " +
                         "LEFT JOIN users u ON u.user_id = m.referee_id " +
                         "LEFT JOIN locations l ON l.location_id = m.location_id " +
                         "LEFT JOIN scores s ON s.match_id = m.match_id " +
                         "LEFT JOIN teams wt ON wt.team_id = s.winner_team_id " +
                         (tournamentFilter != null ? "WHERE m.tournament_id=? " : "") +
                         "ORDER BY m.match_date";
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                if (tournamentFilter != null) ps.setInt(1, Integer.parseInt(tournamentFilter));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("match_id",      rs.getInt("match_id"));
                    row.put("tournament_id", rs.getInt("tournament_id"));
                    row.put("team1_name",    rs.getString("team1_name"));
                    row.put("team2_name",    rs.getString("team2_name"));
                    row.put("referee_name",  rs.getString("referee_name"));
                    row.put("venue_name",    rs.getString("venue_name"));
                    row.put("city",          rs.getString("city"));
                    row.put("match_date",    rs.getString("match_date"));
                    row.put("status",        rs.getString("status"));
                    row.put("team1_score",   rs.getString("team1_score"));
                    row.put("team2_score",   rs.getString("team2_score"));
                    row.put("winner_name",   rs.getString("winner_name"));
                    list.add(row);
                }
            }
            return ok(list);
        });

        post("/matches", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO matches (tournament_id, team1_id, team2_id, referee_id, location_id, match_date) " +
                     "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt   (1, body.get("tournament_id").getAsInt());
                ps.setInt   (2, body.get("team1_id").getAsInt());
                ps.setInt   (3, body.get("team2_id").getAsInt());
                ps.setInt   (4, body.get("referee_id").getAsInt());
                ps.setInt   (5, body.get("location_id").getAsInt());
                ps.setString(6, body.get("match_date").getAsString());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                return ok(Map.of("match_id", keys.getInt(1), "message", "Match scheduled"));
            } catch (Exception e) {
                res.status(400);
                return err(e.getMessage());
            }
        });

        // ────────────────────────────────────────
        // SCORES
        // ────────────────────────────────────────
        post("/scores", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            try (Connection c = getConn()) {
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO scores (match_id, team1_score, team2_score, winner_team_id, updated_by) " +
                    "VALUES (?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE team1_score=VALUES(team1_score), " +
                    "team2_score=VALUES(team2_score), winner_team_id=VALUES(winner_team_id)");
                ps.setInt   (1, body.get("match_id").getAsInt());
                ps.setString(2, body.get("team1_score").getAsString());
                ps.setString(3, body.get("team2_score").getAsString());
                ps.setInt   (4, body.get("winner_team_id").getAsInt());
                ps.setInt   (5, body.get("updated_by").getAsInt());
                ps.executeUpdate();

                // Mark match as completed
                PreparedStatement upd = c.prepareStatement(
                    "UPDATE matches SET status='Completed' WHERE match_id=?");
                upd.setInt(1, body.get("match_id").getAsInt());
                upd.executeUpdate();

                return ok(Map.of("message", "Score updated & match completed"));
            } catch (Exception e) {
                res.status(400);
                return err(e.getMessage());
            }
        });

        // ────────────────────────────────────────
        // STANDINGS
        // ────────────────────────────────────────
        get("/standings/:tournamentId", (req, res) -> {
            List<Map<String,Object>> list = new ArrayList<>();
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM standings WHERE tournament_id=? ORDER BY wins DESC, played DESC")) {
                ps.setInt(1, Integer.parseInt(req.params("tournamentId")));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("team_name", rs.getString("team_name"));
                    row.put("played",    rs.getInt("played"));
                    row.put("wins",      rs.getInt("wins"));
                    row.put("losses",    rs.getInt("losses"));
                    row.put("draws",     rs.getInt("draws"));
                    list.add(row);
                }
            }
            return ok(list);
        });

        // ────────────────────────────────────────
        // USERS (actors)
        // ────────────────────────────────────────
        get("/users", (req, res) -> {
            List<Map<String,Object>> list = new ArrayList<>();
            String role = req.queryParams("role");
            String sql  = "SELECT user_id, name, email, role FROM users" +
                          (role != null ? " WHERE role=?" : "") +
                          " ORDER BY user_id";
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                if (role != null) ps.setString(1, role);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("user_id", rs.getInt("user_id"));
                    row.put("name",    rs.getString("name"));
                    row.put("email",   rs.getString("email"));
                    row.put("role",    rs.getString("role"));
                    list.add(row);
                }
            }
            return ok(list);
        });

        // ────────────────────────────────────────
        // LOCATIONS
        // ────────────────────────────────────────
        get("/locations", (req, res) -> {
            List<Map<String,Object>> list = new ArrayList<>();
            try (Connection c = getConn();
                 PreparedStatement ps = c.prepareStatement("SELECT * FROM locations ORDER BY location_id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("location_id", rs.getInt("location_id"));
                    row.put("venue_name",  rs.getString("venue_name"));
                    row.put("city",        rs.getString("city"));
                    list.add(row);
                }
            }
            return ok(list);
        });

        System.out.println("✅ Tournament API running on http://localhost:4567");
    }
}
