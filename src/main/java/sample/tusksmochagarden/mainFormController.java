package sample.tusksmochagarden;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class mainFormController implements Initializable {

    @FXML
    private AnchorPane main_form, dashboard_form, inventory_form, menu_form, customers_form;

    @FXML
    private Label username, dashboard_NC, dashboard_TI, dashboard_TotalI, dashboard_NSP, menu_total, menu_change;

    @FXML
    private Button dashboard_btn, inventory_btn, menu_btn, customers_btn, logout_btn, menu_receiptBtn, menu_payBtn, menu_removeBtn;

    @FXML
    private TableView<productData> inventory_tableView, menu_tableView;

    @FXML
    private TableColumn<productData, String> inventory_col_productID, inventory_col_productName, inventory_col_type, inventory_col_stock, inventory_col_price, inventory_col_status, inventory_col_date;

    @FXML
    private TableColumn<productData, String> menu_col_productName;

    @FXML
    private TableColumn<productData, Integer> menu_col_quantity;

    @FXML
    private TableColumn<productData, Double> menu_col_price;
    
    @FXML
    private TableView<customersData> customers_tableView;
    
    @FXML
    private TableColumn<customersData, Integer> customers_col_customerID;
    
    @FXML
    private TableColumn<customersData, Double> customers_col_total;
    
    @FXML
    private TableColumn<customersData, java.sql.Date> customers_col_date;
    
    @FXML
    private TableColumn<customersData, String> customers_col_cashier;

    @FXML
    private ImageView inventory_imageView;

    @FXML
    private TextField inventory_productID, inventory_productName, inventory_stock, inventory_price, menu_amount;

    @FXML
    private ComboBox<String> inventory_type, inventory_status;

    @FXML
    private GridPane menu_gridPane;

    @FXML
    private ScrollPane menu_scrollPane;

    @FXML
    private AreaChart<String, Number> dashboard_incomeChart;

    @FXML
    private BarChart<String, Number> dashboard_CustomerChart;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private ObservableList<productData> cardListData = FXCollections.observableArrayList();
    private ObservableList<productData> inventoryListData;
    private ObservableList<productData> menuOrderListData;

    private double totalP;
    private int getid, cID;

    // Dashboard methods
    public void dashboardDisplayNC() {
        String sql = "SELECT COUNT(id) FROM receipt";
        connect = Database.connectDB();
        try {
            int nc = 0;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            if (result.next()) {
                nc = result.getInt("COUNT(id)");
            }
            dashboard_NC.setText(String.valueOf(nc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardDisplayTI() {
        Date date = new Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        String sql = "SELECT SUM(total) FROM receipt WHERE date = '" + sqlDate + "'";
        connect = Database.connectDB();
        try {
            double ti = 0;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            if (result.next()) {
                ti = result.getDouble("SUM(total)");
            }
            dashboard_TI.setText("$" + ti);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardTotalI() {
        String sql = "SELECT SUM(total) FROM receipt";
        connect = Database.connectDB();
        try {
            float ti = 0;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            if (result.next()) {
                ti = result.getFloat("SUM(total)");
            }
            dashboard_TotalI.setText("$" + ti);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardNSP() {
        String sql = "SELECT COUNT(quantity) FROM customer";
        connect = Database.connectDB();
        try {
            int q = 0;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            if (result.next()) {
                q = result.getInt("COUNT(quantity)");
            }
            dashboard_NSP.setText(String.valueOf(q));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardIncomeChart() {
        dashboard_incomeChart.getData().clear();
        String sql = "SELECT date, SUM(total) FROM receipt GROUP BY date ORDER BY TIMESTAMP(date)";
        connect = Database.connectDB();
        XYChart.Series<String, Number> chart = new XYChart.Series<>();
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()) {
                chart.getData().add(new XYChart.Data<>(result.getString(1), result.getFloat(2)));
            }
            dashboard_incomeChart.getData().add(chart);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardCustomerChart() {
        dashboard_CustomerChart.getData().clear();
        String sql = "SELECT date, COUNT(id) FROM receipt GROUP BY date ORDER BY TIMESTAMP(date)";
        connect = Database.connectDB();
        XYChart.Series<String, Number> chart = new XYChart.Series<>();
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()) {
                chart.getData().add(new XYChart.Data<>(result.getString(1), result.getInt(2)));
            }
            dashboard_CustomerChart.getData().add(chart);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inventory methods
    public void inventoryAddBtn() {
        // Enhanced form validation
        if (!validateInventoryForm()) {
            return;
        }
        
        // Validate numeric inputs
        try {
            Integer.parseInt(inventory_stock.getText());
            Double.parseDouble(inventory_price.getText().replace("$", ""));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Stock must be a whole number and price must be a valid number");
            return;
        }
        
        String insertProduct = "INSERT INTO product " +
                "(prod_id, prod_name, type, stock, price, status, image, date) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        
        connect = Database.connectDB();
        
        try {
            // Check if product ID already exists
            String checkProductID = "SELECT prod_id FROM product WHERE prod_id = ?";
            prepare = connect.prepareStatement(checkProductID);
            prepare.setString(1, inventory_productID.getText());
            result = prepare.executeQuery();
            
            if (result.next()) {
                showAlert(Alert.AlertType.ERROR, inventory_productID.getText() + " is already taken");
            } else {
                prepare = connect.prepareStatement(insertProduct);
                prepare.setString(1, inventory_productID.getText().trim());
                prepare.setString(2, inventory_productName.getText().trim());
                prepare.setString(3, inventory_type.getSelectionModel().getSelectedItem());
                
                // Ensure stock is a positive integer
                int stock = Integer.parseInt(inventory_stock.getText());
                if (stock < 0) {
                    showAlert(Alert.AlertType.ERROR, "Stock cannot be negative");
                    return;
                }
                prepare.setInt(4, stock);
                
                // Format price to ensure it's a valid decimal
                double price = Double.parseDouble(inventory_price.getText().replace("$", ""));
                if (price <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Price must be greater than zero");
                    return;
                }
                prepare.setDouble(5, price);
                
                prepare.setString(6, inventory_status.getSelectionModel().getSelectedItem());
                
                String path = data.path;
                if (path != null) {
                    path = path.replace("\\", "\\\\");
                }
                prepare.setString(7, path);
                
                // Get current date for the product
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
                prepare.setDate(8, sqlDate);
                
                prepare.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Product added successfully!");
                inventoryShowData();
                inventoryClearBtn();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Validates inventory form inputs
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInventoryForm() {
        if (inventory_productID.getText().isEmpty() || 
            inventory_productName.getText().isEmpty() || 
            inventory_type.getSelectionModel().getSelectedItem() == null || 
            inventory_stock.getText().isEmpty() || 
            inventory_price.getText().isEmpty() || 
            inventory_status.getSelectionModel().getSelectedItem() == null) {
            
            showAlert(Alert.AlertType.ERROR, "Please fill all blank fields");
            return false;
        }
        
        // Validate product ID format (alphanumeric only)
        if (!inventory_productID.getText().matches("[a-zA-Z0-9]+")) {
            showAlert(Alert.AlertType.ERROR, "Product ID must contain only letters and numbers");
            return false;
        }
        
        // Check that product name isn't just whitespace
        if (inventory_productName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Product name cannot be empty");
            return false;
        }
        
        // If no image is selected, confirm with user
        if (inventory_imageView.getImage() == null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("No Image Selected");
            alert.setHeaderText(null);
            alert.setContentText("You haven't selected a product image. Do you want to continue without an image?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return false;
            }
        }
        
        return true;
    }

    public void inventoryUpdateBtn() {
        if (inventory_productID.getText().isEmpty() || 
            inventory_productName.getText().isEmpty() || 
            inventory_type.getSelectionModel().getSelectedItem() == null || 
            inventory_stock.getText().isEmpty() || 
            inventory_price.getText().isEmpty() || 
            inventory_status.getSelectionModel().getSelectedItem() == null) {
            
            showAlert(Alert.AlertType.ERROR, "Please select an item first");
            return;
        }
        
        String path = data.path;
        if (path != null) {
            path = path.replace("\\", "\\\\");
        }
        
        String updateData = "UPDATE product SET "
                + "prod_name = ?, type = ?, stock = ?, price = ?, "
                + "status = ?, image = ?, date = ? WHERE prod_id = ?";
        
        connect = Database.connectDB();
        
        try {
            // Alert confirmation before updating
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to UPDATE Product ID: " + inventory_productID.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();
            
            if (option.get().equals(ButtonType.OK)) {
                prepare = connect.prepareStatement(updateData);
                prepare.setString(1, inventory_productName.getText());
                prepare.setString(2, inventory_type.getSelectionModel().getSelectedItem());
                prepare.setString(3, inventory_stock.getText());
                
                // Format price to ensure it's a valid decimal
                double price = Double.parseDouble(inventory_price.getText().replace("$", ""));
                prepare.setString(4, String.valueOf(price));
                
                prepare.setString(5, inventory_status.getSelectionModel().getSelectedItem());
                prepare.setString(6, path);
                
                // Get current date for the product update
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
                prepare.setString(7, String.valueOf(sqlDate));
                
                prepare.setString(8, inventory_productID.getText());
                
                prepare.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Successfully Updated!");
                inventoryShowData();
                inventoryClearBtn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inventoryDeleteBtn() {
        if (inventory_productID.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select an item first");
            return;
        }
        
        String deleteData = "DELETE FROM product WHERE prod_id = ?";
        
        connect = Database.connectDB();
        
        try {
            // Alert confirmation before deleting
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to DELETE Product ID: " + inventory_productID.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();
            
            if (option.get().equals(ButtonType.OK)) {
                prepare = connect.prepareStatement(deleteData);
                prepare.setString(1, inventory_productID.getText());
                prepare.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Successfully Deleted!");
                inventoryShowData();
                inventoryClearBtn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inventoryClearBtn() {
        inventory_productID.clear();
        inventory_productName.clear();
        inventory_type.getSelectionModel().clearSelection();
        inventory_stock.clear();
        inventory_price.clear();
        inventory_status.getSelectionModel().clearSelection();
        inventory_imageView.setImage(null);
    }

    public void inventoryImportBtn() {
        FileChooser openFile = new FileChooser();
        openFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Open Image File", "*png", "*jpg"));
        File file = openFile.showOpenDialog(main_form.getScene().getWindow());
        if (file != null) {
            data.path = file.getAbsolutePath();
            var image = new Image(file.toURI().toString(), 120, 127, false, true);
            inventory_imageView.setImage(image);
        }
    }

    public void inventorySelectData() {
        productData prodData = inventory_tableView.getSelectionModel().getSelectedItem();
        int num = inventory_tableView.getSelectionModel().getSelectedIndex();
        
        if ((num - 1) < -1) { // When no item is selected
            return;
        }
        
        inventory_productID.setText(prodData.getProductId());
        inventory_productName.setText(prodData.getProductName());
        inventory_stock.setText(String.valueOf(prodData.getStock()));
        inventory_price.setText(String.valueOf(prodData.getPrice()));
        
        // Set the dropdown selections for type and status
        inventory_type.getSelectionModel().select(prodData.getType());
        inventory_status.getSelectionModel().select(prodData.getStatus());
        
        // Handle the product image
        String path = "file:" + prodData.getImage();
        data.path = prodData.getImage();
        
        if (prodData.getImage() != null && !prodData.getImage().isEmpty()) {
            Image image = new Image(path, 120, 127, false, true);
            inventory_imageView.setImage(image);
        }
    }

    public void inventoryShowData() {
        inventoryListData = inventoryDataList();
        inventory_col_productID.setCellValueFactory(new PropertyValueFactory<>("productId"));
        inventory_col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        inventory_col_type.setCellValueFactory(new PropertyValueFactory<>("type"));
        inventory_col_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        inventory_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        inventory_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        inventory_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        inventory_tableView.setItems(inventoryListData);
    }

    public ObservableList<productData> inventoryDataList() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product";
        connect = Database.connectDB();
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()) {
                listData.add(new productData(
                        result.getInt("id"),
                        result.getString("prod_id"),
                        result.getString("prod_name"),
                        result.getString("type"),
                        result.getInt("stock"),
                        result.getDouble("price"),
                        result.getString("status"),
                        result.getString("image"),
                        result.getDate("date")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listData;
    }

    // Menu methods
    public void menuDisplayCard() {
        cardListData.clear();
        cardListData.addAll(menuGetData());
        menu_gridPane.getChildren().clear();
        int row = 0, column = 0;
        for (productData product : cardListData) {
            try {
                FXMLLoader load = new FXMLLoader();
                load.setLocation(getClass().getResource("cardProduct.fxml"));
                AnchorPane pane = load.load();

                // Get the controller of cardProduct.fxml and pass mainFormController reference
                cardProductController cardC = load.getController();
                cardC.setData(product);
                cardC.setMainFormController(this); // Pass the mainFormController instance

                if (column == 3) {
                    column = 0;
                    row++;
                }
                menu_gridPane.add(pane, column++, row);
                GridPane.setMargin(pane, new Insets(10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ObservableList<productData> menuGetData() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product";
        connect = Database.connectDB();
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()) {
                listData.add(new productData(
                        result.getInt("id"),
                        result.getString("prod_id"),
                        result.getString("prod_name"),
                        result.getString("type"),
                        result.getInt("stock"),
                        result.getDouble("price"),
                        result.getString("image"),
                        result.getDate("date")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listData;
    }

    public void menuSelectOrder() {
        productData prod = menu_tableView.getSelectionModel().getSelectedItem();
        if (prod != null) {
            getid = prod.getId();
        }
    }

    public void menuPayBtn() {
        if (menuOrderListData.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please order first!");
            return;
        }

        if (menu_amount.getText().isEmpty() || menu_amount.getText().equals("0") || menu_amount.getText().equals("0.0")) {
            showAlert(Alert.AlertType.ERROR, "Please input a valid amount!");
            return;
        }
        
        double amount = Double.parseDouble(menu_amount.getText());
        double total = Double.parseDouble(menu_total.getText().replace("$", ""));
        
        if (amount < total) {
            showAlert(Alert.AlertType.ERROR, "Invalid payment amount!");
            return;
        }
        
        // Calculate change and display it
        double change = amount - total;
        menu_change.setText(String.format("$%.2f", change));
        
        // Create receipt
        String insertReceipt = "INSERT INTO receipt (customer_id, total, date, em_username) VALUES (?, ?, ?, ?)";
        connect = Database.connectDB();
        
        try {
            // Alert confirmation before processing
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to proceed with payment?");
            Optional<ButtonType> option = alert.showAndWait();
            
            if (option.get().equals(ButtonType.OK)) {
                prepare = connect.prepareStatement(insertReceipt);
                prepare.setString(1, String.valueOf(data.cID));
                prepare.setString(2, String.valueOf(total));
                
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
                prepare.setString(3, String.valueOf(sqlDate));
                prepare.setString(4, data.username);
                
                prepare.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Payment successful!");
                
                // Clear the order table and reset
                String deleteCustomerData = "DELETE FROM customer WHERE customer_id = " + data.cID;
                prepare = connect.prepareStatement(deleteCustomerData);
                prepare.executeUpdate();
                
                menuShowOrderData();
                menuGetTotal();
                menu_amount.clear();
                menu_change.setText("$0.0");
                
                // Update dashboard data
                if (dashboard_form.isVisible()) {
                    dashboardDisplayNC();
                    dashboardDisplayTI();
                    dashboardTotalI();
                    dashboardNSP();
                    dashboardIncomeChart();
                    dashboardCustomerChart();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void menuRemoveBtn() {
        if (getid == 0 || menuOrderListData.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select an item from the order!");
            return;
        }
        
        String deleteOrder = "DELETE FROM customer WHERE id = " + getid;
        connect = Database.connectDB();
        
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to remove this item?");
            Optional<ButtonType> option = alert.showAndWait();
            
            if (option.get().equals(ButtonType.OK)) {
                prepare = connect.prepareStatement(deleteOrder);
                prepare.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Successfully removed!");
                
                menuShowOrderData();
                menuGetTotal();
                
                // Reset product availability
                String selectProduct = "SELECT prod_id, quantity FROM customer WHERE id = " + getid;
                prepare = connect.prepareStatement(selectProduct);
                result = prepare.executeQuery();
                
                if (result.next()) {
                    String productId = result.getString("prod_id");
                    int quantity = result.getInt("quantity");
                    
                    String updateStock = "UPDATE product SET stock = stock + ? WHERE prod_id = ?";
                    prepare = connect.prepareStatement(updateStock);
                    prepare.setInt(1, quantity);
                    prepare.setString(2, productId);
                    prepare.executeUpdate();
                }
                
                getid = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void menuReceiptBtn() {
        if (menuOrderListData.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No order items to print receipt for!");
            return;
        }
        
        try {
            // Create receipt content
            StringBuilder receipt = new StringBuilder();
            receipt.append("===========================================\n");
            receipt.append("             TUSKS MOCHA GARDEN            \n");
            receipt.append("===========================================\n");
            receipt.append("Date: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n");
            receipt.append("Cashier: ").append(data.username).append("\n");
            receipt.append("Customer ID: ").append(data.cID).append("\n");
            receipt.append("-------------------------------------------\n");
            receipt.append(String.format("%-20s %-8s %-10s\n", "ITEM", "QTY", "PRICE"));
            receipt.append("-------------------------------------------\n");
            
            // Add each item
            for (productData item : menuOrderListData) {
                receipt.append(String.format("%-20s %-8d $%-10.2f\n", 
                    truncateString(item.getProductName(), 20),
                    item.getQuantity(),
                    item.getPrice()));
            }
            
            receipt.append("-------------------------------------------\n");
            receipt.append(String.format("TOTAL%33s\n", String.format("$%.2f", totalP)));
            
            // Add payment info if available
            if (!menu_amount.getText().isEmpty()) {
                double amount = Double.parseDouble(menu_amount.getText().replace("$", "").replace(",", "").trim());
                double change = amount - totalP;
                
                receipt.append(String.format("AMOUNT PAID%28s\n", String.format("$%.2f", amount)));
                receipt.append(String.format("CHANGE%32s\n", String.format("$%.2f", change)));
            }
            
            receipt.append("===========================================\n");
            receipt.append("          THANK YOU FOR YOUR VISIT!        \n");
            receipt.append("===========================================\n");
            
            // Show receipt in a dialog
            Alert receiptDialog = new Alert(Alert.AlertType.INFORMATION);
            receiptDialog.setTitle("Receipt");
            receiptDialog.setHeaderText("Tusks Mocha Garden Receipt");
            
            // Create a monospaced text area for the receipt
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(receipt.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));
            textArea.setPrefWidth(400);
            textArea.setPrefHeight(500);
            
            receiptDialog.getDialogPane().setContent(textArea);
            receiptDialog.showAndWait();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error generating receipt: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Truncates a string to a specified length
     * 
     * @param str The string to truncate
     * @param length The maximum length
     * @return The truncated string
     */
    private String truncateString(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }

    public void menuShowOrderData() {
        menuOrderListData = menuGetOrderList();
        
        menu_col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        menu_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        menu_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        menu_tableView.setItems(menuOrderListData);
    }
    
    private ObservableList<productData> menuGetOrderList() {
        customerID(); // Ensure we have a valid customer ID
        
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM customer WHERE customer_id = " + data.cID;
        
        connect = Database.connectDB();
        
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            
            while (result.next()) {
                productData prod = new productData(
                        result.getInt("id"),
                        result.getString("prod_id"),
                        result.getString("prod_name"),
                        result.getString("type"),
                        result.getInt("quantity"),
                        result.getDouble("price"),
                        result.getString("image"),
                        result.getDate("date")
                );
                
                listData.add(prod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return listData;
    }

    public void menuGetTotal() {
        customerID();
        
        String sql = "SELECT SUM(price) FROM customer WHERE customer_id = " + data.cID;
        connect = Database.connectDB();
        
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            
            if (result.next()) {
                totalP = result.getDouble("SUM(price)");
            }
            
            menu_total.setText(String.format("$%.2f", totalP));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void customerID() {
        String sql = "SELECT MAX(customer_id) FROM receipt";
        connect = Database.connectDB();
        
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            
            if (result.next()) {
                cID = result.getInt("MAX(customer_id)") + 1;
            } else {
                cID = 1;
            }
            
            data.cID = cID;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Customers methods
    public void customersShowData() {
        ObservableList<customersData> customersList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM receipt";
        
        connect = Database.connectDB();
        
        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            
            while (result.next()) {
                customersData customer = new customersData(
                        result.getInt("id"),
                        result.getInt("customer_id"),
                        result.getDouble("total"),
                        result.getDate("date"),
                        result.getString("em_username")
                );
                
                customersList.add(customer);
            }
            
            // Setting up the table column values
            customers_col_customerID.setCellValueFactory(new PropertyValueFactory<>("customerID"));
            customers_col_total.setCellValueFactory(new PropertyValueFactory<>("total"));
            customers_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));
            customers_col_cashier.setCellValueFactory(new PropertyValueFactory<>("emUsername"));
            
            customers_tableView.setItems(customersList);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch forms
    public void switchForm(ActionEvent event) {
        if (event.getSource() == dashboard_btn) {
            dashboard_form.setVisible(true);
            inventory_form.setVisible(false);
            menu_form.setVisible(false);
            customers_form.setVisible(false);
            dashboardDisplayNC();
            dashboardDisplayTI();
            dashboardTotalI();
            dashboardNSP();
            dashboardIncomeChart();
            dashboardCustomerChart();
        } else if (event.getSource() == inventory_btn) {
            dashboard_form.setVisible(false);
            inventory_form.setVisible(true);
            menu_form.setVisible(false);
            customers_form.setVisible(false);
            inventoryShowData();
        } else if (event.getSource() == menu_btn) {
            dashboard_form.setVisible(false);
            inventory_form.setVisible(false);
            menu_form.setVisible(true);
            customers_form.setVisible(false);
            menuDisplayCard();
            menuShowOrderData();
        } else if (event.getSource() == customers_btn) {
            dashboard_form.setVisible(false);
            inventory_form.setVisible(false);
            menu_form.setVisible(false);
            customers_form.setVisible(true);
            customersShowData();
        }
    }

    // Logout method
    public void logout() {
        try {
            showAlert(Alert.AlertType.CONFIRMATION, "Are you sure you want to logout?");
            logout_btn.getScene().getWindow().hide();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("FXMLDocument.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Tusks Mocha Garden");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Display username
    public void displayUsername() {
        String user = data.username;
        username.setText(user.substring(0, 1).toUpperCase() + user.substring(1));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize the inventory type and status combo boxes
            ObservableList<String> typeList = FXCollections.observableArrayList("Beverage", "Meal", "Dessert", "Others");
            inventory_type.setItems(typeList);
            
            ObservableList<String> statusList = FXCollections.observableArrayList("Available", "Unavailable");
            inventory_status.setItems(statusList);
            
            // Initialize the data collections
            menuOrderListData = FXCollections.observableArrayList();
            
            // Configure UI components
            menu_payBtn.setDisable(true); // Disable payment button until valid amount entered
            resetPaymentFields();
            
            // Set up numeric-only text formatters for numeric fields
            setupNumericOnlyFields();
            
            // Load initial data
            displayUsername();
            loadDashboardData();
            inventoryShowData();
            menuDisplayCard();
            customersShowData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up text formatters to only allow numeric input in appropriate fields
     */
    private void setupNumericOnlyFields() {
        // Only allow integers in the stock field
        inventory_stock.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                inventory_stock.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // Allow decimal numbers in price field
        inventory_price.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                inventory_price.setText(oldValue);
            }
        });
        
        // Allow decimal numbers in amount field
        menu_amount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                menu_amount.setText(oldValue);
            }
        });
    }
    
    /**
     * Loads all dashboard data elements
     */
    private void loadDashboardData() {
        try {
            dashboardDisplayNC();
            dashboardDisplayTI();
            dashboardTotalI();
            dashboardNSP();
            dashboardIncomeChart();
            dashboardCustomerChart();
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reset payment fields when order changes
     */
    private void resetPaymentFields() {
        if (menu_amount != null) menu_amount.clear();
        if (menu_change != null) menu_change.setText("$0.00");
        if (menu_payBtn != null) menu_payBtn.setDisable(true);
    }

    private void showAlert(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Handles the amount input and calculates change
     * 
     * @param actionEvent the event that triggered the method
     */
    public void menuAmount(ActionEvent actionEvent) {
        // If no order or no amount entered, do nothing
        if (menu_amount.getText().isEmpty() || totalP == 0) {
            return;
        }
        
        // Process and validate the payment amount
        try {
            // Remove any currency symbol and whitespace
            String cleanAmount = menu_amount.getText()
                .replace("$", "")
                .replace(",", "")
                .trim();
                
            // Validate input is a proper number
            if (!cleanAmount.matches("\\d+(\\.\\d{1,2})?")) {
                showAlert(Alert.AlertType.ERROR, "Please enter a valid amount (e.g., 25.50)");
                return;
            }
            
            double amount = Double.parseDouble(cleanAmount);
            
            // Validate amount is sufficient
            if (amount < totalP) {
                showAlert(Alert.AlertType.ERROR, 
                    String.format("Payment amount $%.2f is less than the total $%.2f", amount, totalP));
                return;
            }
            
            // Calculate and show the change with proper formatting
            double change = amount - totalP;
            menu_change.setText(String.format("$%.2f", change));
            
            // Enable pay button now that a valid amount is entered
            menu_payBtn.setDisable(false);
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Please enter a valid number!");
            menu_amount.clear();
            menu_change.setText("$0.00");
            menu_payBtn.setDisable(true);
        }
    }
    
}
