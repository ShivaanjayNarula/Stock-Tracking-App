package com.stocktracker.database.daos;

import com.stocktracker.database.DatabaseManager;
import java.sql.*;

public class UserDAO {
    // Insert a user into the database
    public static void insertUser(String username, String passwordHash) {
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
            System.out.println("User added!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all users from the database
    public static void getAllUsers() {
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("User: " + rs.getString("username"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}