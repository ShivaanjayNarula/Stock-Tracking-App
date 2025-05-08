package com.stocktracker.database.daos;

import com.stocktracker.database.DatabaseManager;
import java.sql.*;

public class StockDAO {
    public static boolean insertStockData(String symbol, double open, double high,
                                          double low, double close, long volume)
            throws SQLException { // Now throws exception
        String sql = "INSERT INTO stock_data(symbol, open, high, low, close, volume) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.setDouble(2, open);
            pstmt.setDouble(3, high);
            pstmt.setDouble(4, low);
            pstmt.setDouble(5, close);
            pstmt.setLong(6, volume);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        }
    }

    public static ResultSet getHistoricalData(String symbol) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT timestamp, open, high, low, close " +
                        "FROM stock_data WHERE symbol = ? ORDER BY timestamp",
                ResultSet.TYPE_SCROLL_INSENSITIVE,  // Allow bidirectional navigation
                ResultSet.CONCUR_READ_ONLY
        );
        pstmt.setString(1, symbol);
        return pstmt.executeQuery();
    }
}