package sample.tusksmochagarden;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database { // Renamed to follow Java naming conventions (PascalCase)

    // JDBC URL, username, and password
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/tusks_mocha_garden";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "MySql2025";

    public static Connection connectDB() {
        Connection connect = null;
        try {
            // Modern JDBC drivers auto-load, so Class.forName() is optional but harmless
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            System.out.println("Connection successful!"); // Debugging line
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed! Check database/credentials.");
            e.printStackTrace();
        }
        return connect;
    }

    // Test the connection
    public static void main(String[] args) {
        try (Connection conn = connectDB()) {
            if (conn != null) {
                System.out.println("Database connection is valid: " + conn.isValid(5));
            } else {
                System.out.println("Connection is null!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}