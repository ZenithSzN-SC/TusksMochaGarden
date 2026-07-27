-- Database Updates for Admin Functionality
-- Run these SQL commands in your MySQL database

-- Add the is_admin column to the employee table
ALTER TABLE employee ADD COLUMN is_admin BOOLEAN DEFAULT FALSE;

-- Optional: Create an admin user for testing
-- Replace 'your_hashed_password' with the actual hashed password
-- You can generate this by running the application and registering a user with admin privileges

-- Update existing users (optional - set specific users as admin)
-- UPDATE employee SET is_admin = TRUE WHERE username = 'admin';

-- Verify the changes
DESCRIBE employee; 
-- ============================================================
-- tusks-modern redesign migration (applied automatically by the
-- app at startup via SchemaUpdater; kept here for reference)
-- ============================================================

CREATE TABLE IF NOT EXISTS order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  receipt_id INT NOT NULL,
  prod_id VARCHAR(50),
  prod_name VARCHAR(100),
  quantity INT,
  price DOUBLE,
  options VARCHAR(255),
  INDEX idx_receipt (receipt_id)
);

-- Receipt gains order lifecycle + payment metadata
ALTER TABLE receipt ADD COLUMN order_type VARCHAR(20) DEFAULT 'Takeaway';
ALTER TABLE receipt ADD COLUMN payment_method VARCHAR(20) DEFAULT 'Card';
ALTER TABLE receipt ADD COLUMN status VARCHAR(20) DEFAULT 'Prep';
ALTER TABLE receipt ADD COLUMN order_time TIME NULL;

-- Cart lines can carry drink customization (size, temp, sugar, ice, note)
ALTER TABLE customer ADD COLUMN options VARCHAR(255) NULL;

-- Staff activity for the admin Staff Management view
ALTER TABLE employee ADD COLUMN last_active DATETIME NULL;
