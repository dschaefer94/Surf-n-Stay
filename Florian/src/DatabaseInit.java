import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class DatabaseInit {

    private static final String DB_URL = "jdbc:sqlite:surfstay.db";
    private static final String SQL_FILE = "/schema.sql"; // Der Pfad im src-Ordner

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            System.out.println("Lade Datenbankschema aus " + SQL_FILE + " ...");

            // 1. Datei aus dem 'src' Ordner (Classpath) lesen
            InputStream inputStream = DatabaseInit.class.getResourceAsStream(SQL_FILE);

            if (inputStream == null) {
                System.err.println("FEHLER: Konnte schema.sql nicht finden!");
                return;
            }

            // 2. Inhalt in einen String umwandeln
            String sqlContent = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // 3. Den String in einzelne Befehle zerlegen (Trennung durch Semikolon)
            // SQLite mag es nicht, wenn man alles auf einmal sendet.
            String[] commands = sqlContent.split(";");

            // 4. Jeden Befehl einzeln ausführen
            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    try {
                        stmt.execute(command);
                    } catch (SQLException e) {
                        // Ignorieren wir Fehler bei Kommentaren oder leeren Zeilen
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
}