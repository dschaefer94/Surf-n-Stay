package db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:surfstay.db";
    private static final String SQL_FILE = "/schema.sql"; // Der Pfad im src-Ordner

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            System.out.println("Lade Datenbankschema aus " + SQL_FILE + " ...");
            InputStream inputStream = Database.class.getResourceAsStream(SQL_FILE);
            if (inputStream == null) {
                System.err.println("FEHLER: Konnte schema.sql nicht finden!");
                return;
            }
            String sqlContent = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .filter(line -> !line.trim().startsWith("--")) // Entfernt SQL-Kommentare
                    .collect(Collectors.joining(" "));

            String[] commands = sqlContent.split(";");
            for (String command : commands) {
                String trimmedCommand = command.trim();
                if (!trimmedCommand.isEmpty()) {
                    try {
                        stmt.execute(trimmedCommand);
                    } catch (SQLException e) {
                        System.out.println("Info bei SQL-Ausführung: " + e.getMessage());
                    }
                }
            }
            System.out.println("Datenbank erfolgreich initialisiert.");
        } catch (Exception e) {
            System.out.println("Kritischer Fehler bei DB-Init: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}
