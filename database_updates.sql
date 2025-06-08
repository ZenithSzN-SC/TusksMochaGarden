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