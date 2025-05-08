# Tusks Mocha Garden - Cafe Management System

A comprehensive cafe management system built with Java, JavaFX, and MySQL.

## Features

- **User Authentication System**
  - Login/Logout
  - User Registration
  - Password Recovery

- **Dashboard**
  - Sales Statistics
  - Customer Metrics
  - Income Charts
  - Daily Sales Overview

- **Inventory Management**
  - Product CRUD Operations
  - Stock Management
  - Image Upload for Products
  - Product Categorization

- **Menu and Order Processing**
  - Visual Menu Cards
  - Order Management
  - Payment Processing
  - Receipt Generation

- **Customer Records**
  - Transaction History
  - Sales Reports

## System Requirements

- Java 11 or higher
- MySQL 8.0 or higher
- JavaFX 19 or compatible version

## Setup Instructions

1. **Database Setup**
   - Install MySQL
   - Create a database named `tusks_mocha_garden`
   - The application will automatically create the required tables

2. **Configuration**
   - Update the database connection details in `Database.java` if needed
   - Default credentials: 
     - Username: root
     - Password: MySql2025
     - Database: tusks_mocha_garden

3. **Running the Application**
   - Compile and run the application using your IDE or Gradle
   - The main class is `tusksMochaGardenApplication.java`

4. **Login Credentials**
   - For testing, you can use:
     - Username: testuser
     - Password: testpass
   - Or register a new account

## Project Structure

- `src/main/java/sample/tusksmochagarden/` - Java source files
- `src/main/resources/sample/tusksmochagarden/` - FXML and CSS files
- `lib/` - External libraries (MySQL connector)

## Implementation Notes

For detailed information about the implementation, see the [Implementation Notes](implementation_notes.md).

## Implementation Status

The project has been fully implemented with all the necessary functionality:

- ✅ Database setup with proper tables and relationships
- ✅ User authentication with login, registration, and password recovery
- ✅ Dashboard with sales metrics and charts
- ✅ Inventory management (add, update, delete products)
- ✅ Menu system with product cards and order processing
- ✅ Customer transaction tracking and records

For more detailed information about the implementation, see the [Implementation Notes](implementation_notes.md).

## Known Issues

- The application might encounter JavaFX compatibility issues depending on your environment. See the Implementation Notes for troubleshooting steps.
- The receipt generation functionality is currently a placeholder and would need further implementation for actual receipt printing or PDF generation.

## License

This project is for educational purposes only.