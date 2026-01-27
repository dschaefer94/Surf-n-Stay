import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SurfStayServer {

    private static final int PORT = 8080;
    private static final String DB_URL = "jdbc:sqlite:surfstay.db";

    public static void main(String[] args) throws IOException {
        // 1. Datenbank initialisieren (Tabellen bauen)
        DatabaseInit.initialize();

        // 2. Server starten
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // 3. Endpunkte definieren
        server.createContext("/offers", new OfferHandler());
        // Optional: server.createContext("/users", new UserHandler());
        // Optional: server.createContext("/bookings", new BookingHandler());

        server.setExecutor(null); // Standard Executor
        server.start();
        System.out.println("🏄 Surf & Stay Server läuft auf Port " + PORT);
    }

    // -------------------------------------------------------------------
    // HANDLER: Verwaltet Anfragen an /offers
    // -------------------------------------------------------------------
    static class OfferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            System.out.println("Anfrage: " + method + " " + path + (query != null ? "?" + query : ""));

            try {
                switch (method) {
                    case "GET":
                        handleGet(exchange, query);
                        break;
                    case "POST":
                        handlePost(exchange);
                        break;
                    case "PUT":
                        handlePut(exchange);
                        break;
                    case "DELETE":
                        handleDelete(exchange, query);
                        break;
                    default:
                        sendResponse(exchange, 405, "Method not allowed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Server Error: " + e.getMessage());
            }
        }

        // --- 1. GET: Angebote suchen oder auflisten ---
        private void handleGet(HttpExchange exchange, String query) throws IOException, SQLException {
            // Parameter parsen (sehr rudimentär)
            Double lat = null, lon = null, radius = null;
            String ownerId = null;

            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        if (kv[0].equals("lat")) lat = Double.parseDouble(kv[1]);
                        if (kv[0].equals("lon")) lon = Double.parseDouble(kv[1]);
                        if (kv[0].equals("radius")) radius = Double.parseDouble(kv[1]);
                        if (kv[0].equals("owner_id")) ownerId = kv[1];
                    }
                }
            }

            // SQL bauen
            String sql = "SELECT * FROM offers";
            // Filterung: Wenn owner_id da ist, filtern wir danach (für die Anbieter-Ansicht)
            if (ownerId != null) {
                sql += " WHERE owner_id = " + ownerId; // Achtung: Anfällig für SQL Injection, hier nur vereinfacht!
            } else {
                // Für Surfer: Nur veröffentlichte anzeigen
                sql += " WHERE is_published = 1";
            }

            List<String> jsonList = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    // Falls Umkreissuche aktiv ist (lat/lon/radius vorhanden)
                    if (lat != null && lon != null && radius != null) {
                        double offerLat = rs.getDouble("lat");
                        double offerLon = rs.getDouble("lon");
                        double dist = calculateDistance(lat, lon, offerLat, offerLon);
                        if (dist > radius) continue; // Überspringen, wenn zu weit weg
                    }

                    // Datensatz zu JSON-String konvertieren
// Im Server Java Code, in der while(rs.next()) Schleife:

// Innerhalb von handleGet -> while(rs.next()) { ...

                    String json = String.format(
                            "{\"id\":%d, \"owner_id\":%d, \"address\":\"%s\", \"price\":\"%s\", \"lat\":%s, \"lon\":%s, \"is_published\":%d, \"beds\":%d, \"has_sauna\":%d, \"has_fireplace\":%d, \"has_pets\":%d, \"has_internet\":%d}",
                            rs.getInt("id"),
                            rs.getInt("owner_id"),
                            rs.getString("address"),
                            rs.getString("price"),
                            rs.getDouble("lat"),
                            rs.getDouble("lon"),
                            rs.getInt("is_published"),
                            rs.getInt("beds"),
                            rs.getInt("has_sauna"),
                            rs.getInt("has_fireplace"),
                            rs.getInt("has_pets"),
                            rs.getInt("has_internet")
                    );

                    jsonList.add(json);
                }
            }

            String response = "[" + String.join(",", jsonList) + "]";
            sendResponse(exchange, 200, response);
        }

        // --- 2. POST: Neues Angebot erstellen ---

        private void handlePost(HttpExchange exchange) throws IOException, SQLException {
            String body = readBody(exchange);

            String sql = "INSERT INTO offers (owner_id, address, price, beds, has_sauna, has_fireplace, has_pets, has_internet, is_published, lat, lon) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, parseIntSafe(extractJsonValue(body, "owner_id")));
                pstmt.setString(2, extractJsonValue(body, "address"));
                pstmt.setString(3, extractJsonValue(body, "price"));
                pstmt.setInt(4, parseIntSafe(extractJsonValue(body, "beds")));
                pstmt.setInt(5, parseIntSafe(extractJsonValue(body, "has_sauna")));
                pstmt.setInt(6, parseIntSafe(extractJsonValue(body, "has_fireplace")));
                pstmt.setInt(7, parseIntSafe(extractJsonValue(body, "has_pets")));
                pstmt.setInt(8, parseIntSafe(extractJsonValue(body, "has_internet")));
                pstmt.setInt(9, parseIntSafe(extractJsonValue(body, "is_published")));
                pstmt.setDouble(10, parseDoubleSafe(extractJsonValue(body, "lat")));
                pstmt.setDouble(11, parseDoubleSafe(extractJsonValue(body, "lon")));

                pstmt.executeUpdate();
            }
            sendResponse(exchange, 201, "{\"message\": \"Created\"}");
        }

        // --- 3. PUT: Angebot komplett aktualisieren ---
        private void handlePut(HttpExchange exchange) throws IOException, SQLException {
            String body = readBody(exchange);
            System.out.println("PUT empfangen: " + body); // Gut zum Debuggen

            // Wir brauchen zwingend die ID
            String id = extractJsonValue(body, "id");
            if (id == null || id.equals("")) {
                sendResponse(exchange, 400, "{\"error\": \"Missing ID\"}");
                return;
            }

            // Das große Update-Statement
            String sql = "UPDATE offers SET " +
                    "address=?, price=?, beds=?, " +
                    "has_sauna=?, has_fireplace=?, has_pets=?, has_internet=?, " +
                    "is_published=?, lat=?, lon=? " +
                    "WHERE id=?";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Werte aus dem JSON holen und in das SQL einfügen
                pstmt.setString(1, extractJsonValue(body, "address"));
                pstmt.setString(2, extractJsonValue(body, "price"));

                // Zahlen sicher parsen (falls mal was leer ist, nehmen wir 0)
                pstmt.setInt(3, parseIntSafe(extractJsonValue(body, "beds")));
                pstmt.setInt(4, parseIntSafe(extractJsonValue(body, "has_sauna")));
                pstmt.setInt(5, parseIntSafe(extractJsonValue(body, "has_fireplace")));
                pstmt.setInt(6, parseIntSafe(extractJsonValue(body, "has_pets")));
                pstmt.setInt(7, parseIntSafe(extractJsonValue(body, "has_internet")));
                pstmt.setInt(8, parseIntSafe(extractJsonValue(body, "is_published"))); // Hier passiert das Publish/Unpublish

                // Koordinaten (Default auf 0.0 wenn leer)
                pstmt.setDouble(9, parseDoubleSafe(extractJsonValue(body, "lat")));
                pstmt.setDouble(10, parseDoubleSafe(extractJsonValue(body, "lon")));

                // Die ID für die WHERE-Klausel am Ende
                pstmt.setInt(11, Integer.parseInt(id));

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    sendResponse(exchange, 200, "{\"message\": \"Offer updated successfully\"}");
                } else {
                    sendResponse(exchange, 404, "{\"message\": \"Offer not found\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
            }
        }

// --- HILFSMETHODEN (Unten in der Klasse einfügen) ---

        // Hilft, Abstürze zu vermeiden, wenn mal eine Zahl im JSON fehlt
        private int parseIntSafe(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0; // Default Wert
            }
        }

        private double parseDoubleSafe(String value) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        // --- 4. DELETE: Angebot löschen ---
        private void handleDelete(HttpExchange exchange, String query) throws IOException, SQLException {
            String id = null;
            if (query != null && query.startsWith("id=")) {
                id = query.split("=")[1];
            }

            if (id == null) {
                sendResponse(exchange, 400, "Missing ID");
                return;
            }

            String sql = "DELETE FROM offers WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.executeUpdate();
            }
            sendResponse(exchange, 200, "{\"message\": \"Deleted\"}");
        }
    }

    // -------------------------------------------------------------------
    // HILFS-METHODEN
    // -------------------------------------------------------------------

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (Scanner scanner = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }

    // Haversine Formel zur Berechnung der Entfernung zwischen zwei Koordinaten in km
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius der Erde in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Hilfsmethode um Werte aus einem JSON String zu fummeln (ohne Gson/Jackson)
    // Erwartet flaches JSON: {"key":"value", "key2":123}
    private static String extractJsonValue(String json, String key) {
        // Entfernt { } und "
        String clean = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] pairs = clean.split(",");

        for (String pair : pairs) {
            String[] kv = pair.split(":");
            // Wir suchen den Key. Trim() entfernt Leerzeichen
            if (kv.length >= 2 && kv[0].trim().equals(key)) {
                return kv[1].trim();
            }
        }
        return ""; // Nicht gefunden
    }
}