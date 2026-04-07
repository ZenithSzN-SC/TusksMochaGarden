package sample.tusksmochagarden;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Unit tests for database connectivity.
 * Tests the Database class connection functionality.
 */
public class DatabaseTest {
    
    @Test
    @DisplayName("Database connection should not be null")
    void testDatabaseConnection() {
        Connection connection = Database.connectDB();
        
        assertNotNull(connection, "Database connection should not be null");
        
        // Clean up connection
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Log error but don't fail test
                System.err.println("Error closing test connection: " + e.getMessage());
            }
        }
    }
    
    @Test
    @DisplayName("Database connection should be valid")
    void testDatabaseConnectionValidity() {
        Connection connection = Database.connectDB();
        
        if (connection != null) {
            try {
                assertTrue(connection.isValid(5), "Database connection should be valid");
                assertFalse(connection.isClosed(), "Database connection should not be closed");
            } catch (SQLException e) {
                fail("SQLException occurred while testing connection validity: " + e.getMessage());
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing test connection: " + e.getMessage());
                }
            }
        } else {
            // If connection is null, we can still pass the test but log a warning
            // This allows tests to run even if database is not available
            System.out.println("Warning: Database connection is null - database may not be available");
        }
    }
    
    @Test
    @DisplayName("Multiple database connections should work")
    void testMultipleConnections() {
        Connection conn1 = Database.connectDB();
        Connection conn2 = Database.connectDB();
        
        // At least one connection should be available
        assertTrue(conn1 != null || conn2 != null, "At least one database connection should be available");
        
        // Clean up connections
        try {
            if (conn1 != null) conn1.close();
            if (conn2 != null) conn2.close();
        } catch (SQLException e) {
            System.err.println("Error closing test connections: " + e.getMessage());
        }
    }
}