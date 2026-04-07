package sample.tusksmochagarden;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;

/**
 * Unit tests for productData class.
 * Tests the product data model functionality and validation.
 */
public class ProductDataTest {
    
    @Test
    @DisplayName("Product creation with full constructor should work correctly")
    void testProductCreationFullConstructor() {
        // Arrange
        Integer id = 1;
        String productId = "PROD001";
        String productName = "Coffee";
        String type = "Drinks";
        Integer stock = 50;
        Double price = 3.50;
        String status = "Available";
        String image = "/images/coffee.jpg";
        Date date = new Date(System.currentTimeMillis());
        
        // Act
        productData product = new productData(id, productId, productName, type, stock, price, status, image, date);
        
        // Assert
        assertEquals(id, product.getId());
        assertEquals(productId, product.getProductId());
        assertEquals(productName, product.getProductName());
        assertEquals(type, product.getType());
        assertEquals(stock, product.getStock());
        assertEquals(price, product.getPrice());
        assertEquals(status, product.getStatus());
        assertEquals(image, product.getImage());
        assertEquals(date, product.getDate());
        assertNull(product.getQuantity()); // Should be null for full constructor
    }
    
    @Test
    @DisplayName("Product creation with order constructor should work correctly")
    void testProductCreationOrderConstructor() {
        // Arrange
        Integer id = 2;
        String productId = "PROD002";
        String productName = "Sandwich";
        String type = "Meals";
        Integer quantity = 2;
        Double price = 8.99;
        String image = "/images/sandwich.jpg";
        Date date = new Date(System.currentTimeMillis());
        
        // Act
        productData product = new productData(id, productId, productName, type, quantity, price, image, date);
        
        // Assert
        assertEquals(id, product.getId());
        assertEquals(productId, product.getProductId());
        assertEquals(productName, product.getProductName());
        assertEquals(type, product.getType());
        assertEquals(quantity, product.getQuantity());
        assertEquals(price, product.getPrice());
        assertEquals(image, product.getImage());
        assertEquals(date, product.getDate());
        assertNull(product.getStock()); // Should be null for order constructor
        assertNull(product.getStatus()); // Should be null for order constructor
    }
    
    @Test
    @DisplayName("Product with valid price should be accepted")
    void testValidPrice() {
        Double validPrice = 5.99;
        productData product = new productData(1, "PROD001", "Test Product", "Drinks", 10, validPrice, "Available", "/images/test.jpg", new Date(System.currentTimeMillis()));
        
        assertEquals(validPrice, product.getPrice());
        assertTrue(product.getPrice() > 0, "Price should be positive");
    }
    
    @Test
    @DisplayName("Product with valid stock should be accepted")
    void testValidStock() {
        Integer validStock = 25;
        productData product = new productData(1, "PROD001", "Test Product", "Drinks", validStock, 5.99, "Available", "/images/test.jpg", new Date(System.currentTimeMillis()));
        
        assertEquals(validStock, product.getStock());
        assertTrue(product.getStock() >= 0, "Stock should be non-negative");
    }
    
    @Test
    @DisplayName("Product data should handle null values gracefully")
    void testNullValues() {
        // Test with some null values - this tests how the class handles edge cases
        productData product = new productData(null, null, null, null, null, null, null, null, null);
        
        assertNull(product.getId());
        assertNull(product.getProductId());
        assertNull(product.getProductName());
        assertNull(product.getType());
        assertNull(product.getStock());
        assertNull(product.getPrice());
        assertNull(product.getStatus());
        assertNull(product.getImage());
        assertNull(product.getDate());
    }
}