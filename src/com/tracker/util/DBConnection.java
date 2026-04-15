package com.tracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DB_URL   = "jdbc:mysql://localhost:3306/assignment_tracker?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";      // ← change this
    private static final String PASSWORD = "1234";      // ← change this

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                System.out.println("[DB] Connected successfully.");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver missing.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB. Check credentials.", e);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing: " + e.getMessage());
        }
    }

    private DBConnection() {}
}