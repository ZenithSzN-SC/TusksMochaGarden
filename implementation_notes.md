# Tusks Mocha Garden Implementation Notes

## Overview
This document outlines the implementation steps taken to complete the Tusks Mocha Garden Cafe Shop Management System. The application is built with Java, JavaFX for the UI, and MySQL for data storage.

## Database Setup
The system requires several tables to function properly:

1. **employee** - Staff accounts and authentication
2. **product** - Inventory items for sale in the cafe
3. **customer** - Temporary storage for customer orders (items in cart)
4. **receipt** - Completed transactions

The database structure needed adjustments to align with the application code. The original database had tables with different names and structures than what the application expected.

### Tables Created:
- `product` - Matches the productData model class
- `customer` - For tracking customer orders 
- `receipt` - For completed transactions

## Implementation Tasks

### 1. Database Alignment
- Created the product table to match the code expectations
- Created the customer table for holding current order items
- Created the receipt table for storing completed transactions

### 2. Inventory Management Implementation
- Implemented `inventoryAddBtn()` method to add new products to inventory
- Implemented `inventoryUpdateBtn()` method to update existing products
- Implemented `inventoryDeleteBtn()` method to remove products
- Implemented `inventorySelectData()` method to populate form when a product is selected

### 3. Menu Order Processing Implementation
- Implemented `menuShowOrderData()` to display customer's current order
- Implemented `menuGetTotal()` to calculate the total price of the order
- Implemented `customerID()` to generate and retrieve the customer ID for receipts
- Implemented `menuPayBtn()` to finalize the order and create a receipt
- Implemented `menuRemoveBtn()` to remove items from the order
- Implemented `menuAmount()` to handle payment amount and calculate change

### 4. Customer Records Implementation
- Implemented `customersShowData()` to display all transactions in the customer table

### 5. UI Enhancements
- Initialized product type and status dropdown lists with appropriate values
- Set up proper initialization of all data models and UI components

## Features Implemented

1. **User Authentication**
   - Login with username and password
   - Account registration
   - Password recovery

2. **Dashboard**
   - Display of total sales metrics
   - Charts showing income and customer data
   - Statistics for daily sales

3. **Inventory Management**
   - Adding new products
   - Updating existing products
   - Deleting products
   - Image upload for products

4. **Menu and Order Processing**
   - Product cards display for menu items
   - Add to cart functionality
   - Order management
   - Payment processing with change calculation

5. **Customer Records**
   - Viewing transaction history
   - Receipt generation
   
## How to Test

1. **Login**
   - Use test account: username "testuser", password "testpass"
   - Or create a new account on the registration form

2. **Inventory Management**
   - Add new products with details and images
   - Update existing products
   - Remove products

3. **Menu Order Processing**
   - Browse products in menu
   - Add items to cart using quantity spinners
   - Process payment
   - View receipt

4. **Dashboard and Reports**
   - View updated sales metrics on dashboard
   - Check customer transaction history

## Running the Application

The application uses Gradle for build management and JavaFX for the UI. To run the application:

1. Make sure you have Java JDK 17 or higher installed
2. Set up the MySQL database as described in the Database Setup section
3. Run the application using Gradle:
   ```
   ./gradlew run
   ```

### Troubleshooting

If you encounter JavaFX-related errors when running the application:

1. Make sure the JavaFX and JDK versions are compatible. This project is configured to use JavaFX 19.
2. If using an IDE like IntelliJ IDEA or Eclipse, make sure to:
   - Add the JavaFX SDK to your project
   - Configure VM options to include: `--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing`

3. If specific environment issues persist, consider using a pre-configured JavaFX environment like:
   - [IntelliJ IDEA with JavaFX plugin](https://www.jetbrains.com/help/idea/javafx.html)
   - [e(fx)clipse for Eclipse](https://www.eclipse.org/efxclipse/index.html)
   - [Scene Builder](https://gluonhq.com/products/scene-builder/) for visual editing of FXML files
