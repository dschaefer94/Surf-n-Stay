import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseInit {

    private static final String DB_URL = "jdbc:sqlite:surfstay.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Tabellen erstellen (wie vorher)
            String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE,
                    display_name TEXT
                );
            """;
            stmt.execute(createUsers);

            String createOffers = """
                CREATE TABLE IF NOT EXISTS offers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_id INTEGER NOT NULL,
                    lat REAL,
                    lon REAL,
                    address TEXT,
                    price TEXT,
                    beds INTEGER,
                    start_date TEXT,
                    end_date TEXT,
                    has_sauna INTEGER DEFAULT 0,
                    has_fireplace INTEGER DEFAULT 0,
                    is_smoker INTEGER DEFAULT 0,
                    has_pets INTEGER DEFAULT 0,
                    has_internet INTEGER DEFAULT 0,
                    is_published INTEGER DEFAULT 0
                );
            """;
            stmt.execute(createOffers);

            // 2. Dummy Daten einfügen (Nur wenn Tabelle leer ist)
            insertDummyData(conn);

            System.out.println("Datenbank bereit & Testdaten geladen.");

        } catch (SQLException e) {
            System.out.println("Datenbank-Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertDummyData(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM offers");
        rs.next();
        int count = rs.getInt(1);

        if (count == 0) {
            System.out.println("Tabelle leer. Füge Testdaten hinzu...");

            String sql = "INSERT INTO offers (owner_id, address, lat, lon, price, beds, has_sauna, has_fireplace, has_internet, is_published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Angebot 1: Hvide Sande (Deins, Online)
                pstmt.setInt(1, 1); // Owner ID 1 (DU)
                pstmt.setString(2, "Hvide Sande, Dänemark");
                pstmt.setDouble(3, 56.00);
                pstmt.setDouble(4, 8.13);
                pstmt.setString(5, "25 EUR");
                pstmt.setInt(6, 2);
                pstmt.setInt(7, 1); // Sauna Ja
                pstmt.setInt(8, 1); // Kamin Ja
                pstmt.setInt(9, 1); // Internet Ja
                pstmt.setInt(10, 1); // Published
                pstmt.executeUpdate();

                // Angebot 2: Søndervig (Deins, ENTWURF)
                pstmt.setInt(1, 1); // Owner ID 1 (DU)
                pstmt.setString(2, "Søndervig Beach");
                pstmt.setDouble(3, 56.12);
                pstmt.setDouble(4, 8.11);
                pstmt.setString(5, "Kiste Bier");
                pstmt.setInt(6, 4);
                pstmt.setInt(7, 0);
                pstmt.setInt(8, 0);
                pstmt.setInt(9, 0);
                pstmt.setInt(10, 0); // NICHT Published (Entwurf)
                pstmt.executeUpdate();

                // Angebot 3: Klitmøller (Fremd, Online)
                pstmt.setInt(1, 2); // Owner ID 2 (Jemand anderes)
                pstmt.setString(2, "Cold Hawaii, Klitmøller");
                pstmt.setDouble(3, 57.04);
                pstmt.setDouble(4, 8.56);
                pstmt.setString(5, "50 EUR");
                pstmt.setInt(6, 1);
                pstmt.setInt(7, 1);
                pstmt.setInt(8, 0);
                pstmt.setInt(9, 1);
                pstmt.setInt(10, 1); // Published
                pstmt.executeUpdate();
            }
        }
    }
}