package dao;

import db.Database;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class UserDao {
    public static User selectUser(int id) throws SQLException {
        String sql = "SELECT username, display_name FROM users WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(id);
                    user.setUsername(rs.getString("username"));
                    user.setDisplayName(rs.getString("display_name"));
                    return user;
                } else {
                    throw new NoSuchElementException("Benutzer mit ID " + id + " existiert nicht.");
                }
            }
        }
    }

    public static int insertUser(User u) throws SQLException {
        final String SQL = "INSERT INTO users (username, display_name) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getDisplayName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new SQLException("User wurde angelegt, aber keine ID generiert.");
        }
    }
    public static boolean updateUser(User u) throws SQLException {
        final String SQL = "UPDATE users SET username = ?, display_name = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getDisplayName());
            ps.setInt(3, u.getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }
    public static void pseudoDeleteUser(int userId) throws SQLException {
        String updateSql = "UPDATE offers SET owner_id = 1 WHERE owner_id = ?";
        String deleteSql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql);
                 PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                psUpdate.setInt(1, userId);
                psUpdate.executeUpdate();
                psDelete.setInt(1, userId);
                int affected = psDelete.executeUpdate();
                if (affected == 0) throw new SQLException("User nicht gefunden.");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
