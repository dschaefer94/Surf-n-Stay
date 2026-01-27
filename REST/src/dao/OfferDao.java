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

        String sql = "SELECT id, * FROM offers";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {


            while (rs.next()) {
                Offer offer = new Offer();

                offer.setId(rs.getLong("id"));
                offer.setOwnerId(rs.getLong("owner_id"));
                offer.setLat(rs.getDouble("lat"));
                offer.setLon(rs.getDouble("lon"));
                offer.setAddress(rs.getString("address"));
                offer.setPrice(rs.getString("price"));
                offer.setBeds(rs.getInt("beds"));
                offer.setStartDate(rs.getString("start_date"));
                offer.setHasFireplace(rs.getInt("has_fireplace") == 1);
                offer.setSmoker(rs.getInt("is_smoker") == 1);
                offer.setHasInternet(rs.getInt("has_internet") == 1);
                offer.setPublished(rs.getInt("is_published") == 1);

                list.add(offer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
