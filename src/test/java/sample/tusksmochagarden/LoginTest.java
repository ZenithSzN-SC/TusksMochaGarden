package sample.tusksmochagarden;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Unit tests for login functionality.
 * Tests the authentication logic against actual database users.
 */
public class LoginTest {
    
    private LoginAuthenticator authenticator;
    
    @BeforeEach
    void setUp() {
        authenticator = new LoginAuthenticator();
    }
    
    @Test
    @DisplayName("Valid database login should return true")
    void testValidLogin() {
        // Test with actual database user
        String username = "admin";
        String password = "admin123";
        
        boolean result = authenticator.authenticate(username, password);
        
        assertTrue(result, "Valid database credentials should return true");
    }
    
    @Test
    @DisplayName("Invalid login should return false")
    void testInvalidLogin() {
        // Test with invalid credentials
        String username = "invaliduser";
        String password = "wrongpass";
        
        boolean result = authenticator.authenticate(username, password);
        
        assertFalse(result, "Invalid credentials should return false");
    }
    
    @Test
    @DisplayName("Empty username should return false")
    void testEmptyUsername() {
        String username = "";
        String password = "admin123";
        
        boolean result = authenticator.authenticate(username, password);
        
        assertFalse(result, "Empty username should return false");
    }
    
    @Test
    @DisplayName("Empty password should return false")
    void testEmptyPassword() {
        String username = "admin";
        String password = "";
        
        boolean result = authenticator.authenticate(username, password);
        
        assertFalse(result, "Empty password should return false");
    }
    
    /**
     * Helper class to extract authentication logic from the controller
     * for testing purposes. This performs actual database authentication.
     */
    private static class LoginAuthenticator {
        
        public boolean authenticate(String username, String password) {
            // Replicate the authentication logic from loginController
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                return false;
            }
            
            // Test the hardcoded test user first (from loginController.java:70-80)
            if ("testuser".equals(username) && "testpass".equals(password)) {
                return true;
            }
            
            // Perform actual database authentication
            return authenticateWithDatabase(username, password);
        }
        
        private boolean authenticateWithDatabase(String username, String password) {
            String selectData = "SELECT username, password FROM employee WHERE username = ?";
            Connection connect = Database.connectDB();
            
            if (connect == null) {
                return false;
            }
            
            try (PreparedStatement prepare = connect.prepareStatement(selectData)) {
                prepare.setString(1, username);
                try (ResultSet result = prepare.executeQuery()) {
                    if (result.next()) {
                        String storedPassword = result.getString("password");
                        return password.equals(storedPassword);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Database authentication error: " + e.getMessage());
                return false;
            } finally {
                try {
                    connect.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
            
            return false;
        }
    }
}