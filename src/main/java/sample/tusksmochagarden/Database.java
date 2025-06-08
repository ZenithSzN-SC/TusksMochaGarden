package sample.tusksmochagarden;

import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Database connection manager for the Tusks Mocha Garden application.
 * Handles database connectivity with connection pooling and automatic retry.
 */
public class Database {

    // JDBC URL, username, and password
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/tusks_mocha_garden";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "MySql2025";
    
    // Connection pool settings
    private static final int MAX_POOL_SIZE = 10;
    private static final int CONNECTION_TIMEOUT = 5; // seconds
    
    // Connection pool storage
    private static final ConcurrentHashMap<Connection, Long> connectionPool = new ConcurrentHashMap<>(MAX_POOL_SIZE);
    private static final ScheduledExecutorService connectionCleaner = Executors.newSingleThreadScheduledExecutor();
    
    // Initialize connection pool management
    static {
        // Schedule cleanup of idle connections every 5 minutes
        connectionCleaner.scheduleAtFixedRate(Database::cleanupIdleConnections, 5, 5, TimeUnit.MINUTES);
        
        // Shutdown hook to close all connections when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            connectionCleaner.shutdown();
            closeAllConnections();
        }));
    }

    /**
     * Get a database connection from the pool or create a new one if needed.
     * 
     * @return A valid database connection
     */
    public static Connection connectDB() {
        // First try to get a connection from the pool
        Connection connection = getConnectionFromPool();
        if (connection != null) {
            return connection;
        }
        
        // If pool is empty or no valid connections, create a new one
        try {
            // Modern JDBC drivers auto-load, so Class.forName() is optional but harmless
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Try to connect with retry logic
            for (int attempt = 0; attempt < 3; attempt++) {
                try {
                    connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                    connectionPool.put(connection, System.currentTimeMillis());
                    return connection;
                } catch (SQLException e) {
                    if (attempt == 2) throw e; // Throw on last attempt
                    // Wait before retrying
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            showErrorAlert("Database Error", "MySQL JDBC Driver not found!");
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Connection failed! Check database/credentials.");
            System.err.println("Connection failed! Check database/credentials.");
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get an existing connection from the pool if available
     */
    private static Connection getConnectionFromPool() {
        for (Connection conn : connectionPool.keySet()) {
            try {
                if (conn != null && !conn.isClosed() && conn.isValid(CONNECTION_TIMEOUT)) {
                    // Update the timestamp to mark it as recently used
                    connectionPool.put(conn, System.currentTimeMillis());
                    return conn;
                }
            } catch (SQLException e) {
                // Connection is invalid, remove it from the pool
                connectionPool.remove(conn);
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
        return null;
    }
    
    /**
     * Clean up idle connections that haven't been used recently
     */
    private static void cleanupIdleConnections() {
        final long idleTimeout = 10 * 60 * 1000; // 10 minutes in milliseconds
        final long currentTime = System.currentTimeMillis();
        
        connectionPool.forEach((connection, timestamp) -> {
            if (currentTime - timestamp > idleTimeout) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing idle connection: " + e.getMessage());
                } finally {
                    connectionPool.remove(connection);
                }
            }
        });
    }
    
    /**
     * Close all connections in the pool
     */
    private static void closeAllConnections() {
        connectionPool.forEach((connection, timestamp) -> {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        });
        connectionPool.clear();
    }
    
    /**
     * Display an error alert to the user
     */
    private static void showErrorAlert(String title, String content) {
        // Only show UI alerts if we're in a JavaFX application thread
        if (javafx.application.Platform.isFxApplicationThread()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }

    /**
     * Test the database connection
     */
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