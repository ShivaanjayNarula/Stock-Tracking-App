package com.stocktracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // Path to your SQLite database file
    private static final String DB_URL = "jdbc:sqlite:stocks.db";

    // Initialize the database and create tables
    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create tables if they don't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS stock_data (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "symbol TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "open REAL NOT NULL, " +
                    "high REAL NOT NULL, " +
                    "low REAL NOT NULL, " +
                    "close REAL NOT NULL, " +
                    "volume INTEGER NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS alerts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "symbol TEXT NOT NULL, " +
                    "target_price REAL NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id))");

            System.out.println("Database initialized!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Get a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}