package dao;

import model.Offer;
import db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class OfferDao {
    public static List<Offer> selectOffers(final String OWNER_ID) throws SQLException {
        List<Offer> list = new ArrayList<>();
        String sql = "SELECT * FROM offers";
        boolean hasId = !OWNER_ID.isEmpty();
        if (hasId) {
            sql += " WHERE owner_id = ?";
            try {
                Integer.parseInt(OWNER_ID);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Die ID '" + OWNER_ID + "' ist keine Zahl.");
            }
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasId) {
                ps.setInt(1, Integer.parseInt(OWNER_ID));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Offer offer = new Offer();
                    offer.setId(rs.getInt("id"));
                    offer.setOwnerId(rs.getInt("owner_id"));
                    offer.setLat(rs.getDouble("lat"));
                    offer.setLon(rs.getDouble("lon"));
                    offer.setPrice(rs.getString("price"));
                    offer.setBeds(rs.getInt("beds"));
                    offer.setStartDate(rs.getString("start_date"));
                    offer.setEndDate(rs.getString("end_date"));
                    offer.setHasSauna(rs.getInt("has_sauna") == 1);
                    offer.setHasFireplace(rs.getInt("has_fireplace") == 1);
                    offer.setSmoker(rs.getInt("is_smoker") == 1);
                    offer.setHasPets(rs.getInt("has_pets") == 1);
                    offer.setHasInternet(rs.getInt("has_internet") == 1);
                    offer.setPublished(rs.getInt("is_published") == 1);
                    list.add(offer);
                }
            }
        }
        if (hasId && list.isEmpty()) {
            throw new NoSuchElementException("Keine Angebote des Users gefunden.");
        }
        return list;
    }

    public static int insertOffer(Offer o) throws SQLException {
        final String SQL = """
                INSERT INTO offers (
                    owner_id, lat, lon, price, beds, start_date, end_date,
                    has_sauna, has_fireplace, is_smoker, has_pets, has_internet, is_published
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getOwnerId());
            ps.setDouble(2, o.getLat());
            ps.setDouble(3, o.getLon());
            ps.setString(4, o.getPrice());
            ps.setInt(5, o.getBeds());
            ps.setString(6, o.getStartDate());
            ps.setString(7, o.getEndDate());
            ps.setInt(8, o.getHasSauna() ? 1 : 0);
            ps.setInt(9, o.getHasFireplace() ? 1 : 0);
            ps.setInt(10, o.getSmoker() ? 1 : 0);
            ps.setInt(11, o.getHasPets() ? 1 : 0);
            ps.setInt(12, o.getHasInternet() ? 1 : 0);
            ps.setInt(13, o.getPublished() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new SQLException("Datensatz wurde eingefügt, aber keine ID erhalten.");
        }
    }
    public static boolean updateOffer(Offer o) throws SQLException {
        final String SQL = """
            UPDATE offers SET 
                owner_id = ?, lat = ?, lon = ?, price = ?, beds = ?, 
                start_date = ?, end_date = ?, has_sauna = ?, has_fireplace = ?, 
                is_smoker = ?, has_pets = ?, has_internet = ?, is_published = ?
            WHERE id = ?
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, o.getOwnerId());
            ps.setDouble(2, o.getLat());
            ps.setDouble(3, o.getLon());
            ps.setString(4, o.getPrice());
            ps.setInt(5, o.getBeds());
            ps.setString(6, o.getStartDate());
            ps.setString(7, o.getEndDate());
            ps.setInt(8, o.getHasSauna() ? 1 : 0);
            ps.setInt(9, o.getHasFireplace() ? 1 : 0);
            ps.setInt(10, o.getSmoker() ? 1 : 0);
            ps.setInt(11, o.getHasPets() ? 1 : 0);
            ps.setInt(12, o.getHasInternet() ? 1 : 0);
            ps.setInt(13, o.getPublished() ? 1 : 0);
            ps.setInt(14, o.getId());
            return ps.executeUpdate() > 0;
        }
    }
    public static boolean pseudoDeleteOffer(int offerId) throws SQLException {
        String sql = "UPDATE offers SET owner_id = 1 WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }
}
