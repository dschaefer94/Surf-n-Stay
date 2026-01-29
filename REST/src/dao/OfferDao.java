package dao;

import model.Offer;
import db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OfferDao {
    public static List<Offer> getAllOffers() {
        List<Offer> list = new ArrayList<>();
        String sql = """
                    SELECT id, owner_id, lat, lon, address, price, beds, start_date, end_date,
                           has_pets, has_fireplace, is_smoker, has_internet, has_sauna, is_published
                    FROM offers
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Offer offer = new Offer();
                offer.setId(rs.getInt("id"));
                offer.setOwnerId(rs.getInt("owner_id"));
                offer.setLat(rs.getDouble("lat"));
                offer.setLon(rs.getDouble("lon"));
                offer.setAddress(rs.getString("address"));
                offer.setPrice(rs.getString("price"));
                offer.setBeds(rs.getInt("beds"));
                offer.setStartDate(rs.getString("start_date"));
                offer.setEndDate(rs.getString("end_date"));
                offer.setHasPets(rs.getInt("has_pets") == 1);
                offer.setHasFireplace(rs.getInt("has_fireplace") == 1);
                offer.setSmoker(rs.getInt("is_smoker") == 1);
                offer.setHasInternet(rs.getInt("has_internet") == 1);
                offer.setHasSauna(rs.getInt("has_sauna") == 1);
                offer.setPublished(rs.getInt("is_published") == 1);
                list.add(offer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int insertOffer(Offer o) {
        final String SQL = """
                INSERT INTO offers (
                    owner_id, lat, lon, address, price, beds, start_date, end_date,
                    has_sauna, has_fireplace, is_smoker, has_pets, has_internet, is_published
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, o.getOwnerId());
            ps.setDouble(2, o.getLat());
            ps.setDouble(3, o.getLon());
            ps.setString(4, o.getAddress());
            ps.setString(5, o.getPrice());
            ps.setInt(6, o.getBeds());
            ps.setString(7, o.getStartDate());
            ps.setString(8, o.getEndDate());
            ps.setInt(9, o.getHasSauna() ? 1 : 0);
            ps.setInt(10, o.getHasFireplace() ? 1 : 0);
            ps.setInt(11, o.getSmoker() ? 1 : 0);
            ps.setInt(12, o.getHasPets() ? 1 : 0);
            ps.setInt(13, o.getHasInternet() ? 1 : 0);
            ps.setInt(14, o.getPublished() ? 1 : 0);

            int affected = ps.executeUpdate();
            if (affected != 1) {
                throw new RuntimeException("Insert offers: affected rows=" + affected);
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new RuntimeException("Datensatz wurde nicht eingefügt.");

        } catch (SQLException e) {
            throw new RuntimeException("SQL-Exception", e);
        }
    }
}
