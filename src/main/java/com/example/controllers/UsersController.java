package com.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import sample.tusksmochagarden.Database;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class UsersController implements Initializable {

    @FXML
    private TableView<UserData> userTable;
    
    @FXML
    private TableColumn<UserData, Integer> idColumn;
    
    @FXML
    private TableColumn<UserData, String> usernameColumn;
    
    @FXML
    private TableColumn<UserData, String> passwordColumn;
    
    @FXML
    private TableColumn<UserData, Boolean> isAdminColumn;
    
    @FXML
    private Label usersErrorLabel;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        isAdminColumn.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));
        
        // Load user data
        loadUsers();
    }

    private void loadUsers() {
        try {
            ObservableList<UserData> userList = FXCollections.observableArrayList();
            String sql = "SELECT id, username, password, is_admin FROM employee";
            
            connect = Database.connectDB();
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            
            while (result.next()) {
                UserData user = new UserData(
                    result.getInt("id"),
                    result.getString("username"),
                    "********", // Hide actual password
                    result.getBoolean("is_admin")
                );
                userList.add(user);
            }
            
            userTable.setItems(userList);
            
        } catch (Exception e) {
            showError("Error loading users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeDatabaseResources();
        }
    }

    @FXML
    private void addUser(ActionEvent event) {
        // Create a dialog to add a new user
        Dialog<UserData> dialog = createUserDialog("Add User", null);
        Optional<UserData> result = dialog.showAndWait();
        
        result.ifPresent(user -> {
            try {
                String sql = "INSERT INTO employee (username, password, is_admin, hire_date) VALUES (?, ?, ?, NOW())";
                connect = Database.connectDB();
                prepare = connect.prepareStatement(sql);
                prepare.setString(1, user.getUsername());
                prepare.setString(2, hashPassword(user.getPassword()));
                prepare.setBoolean(3, user.getIsAdmin());
                
                int rowsAffected = prepare.executeUpdate();
                if (rowsAffected > 0) {
                    showInfo("User added successfully!");
                    loadUsers(); // Refresh the table
                } else {
                    showError("Failed to add user.");
                }
                
            } catch (Exception e) {
                showError("Error adding user: " + e.getMessage());
                e.printStackTrace();
            } finally {
                closeDatabaseResources();
            }
        });
    }

    @FXML
    private void removeUser(ActionEvent event) {
        UserData selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Please select a user to remove.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete user: " + selectedUser.getUsername() + "?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String sql = "DELETE FROM employee WHERE id = ?";
                connect = Database.connectDB();
                prepare = connect.prepareStatement(sql);
                prepare.setInt(1, selectedUser.getId());
                
                int rowsAffected = prepare.executeUpdate();
                if (rowsAffected > 0) {
                    showInfo("User deleted successfully!");
                    loadUsers(); // Refresh the table
                } else {
                    showError("Failed to delete user.");
                }
                
            } catch (Exception e) {
                showError("Error deleting user: " + e.getMessage());
                e.printStackTrace();
            } finally {
                closeDatabaseResources();
            }
        }
    }

    @FXML
    private void editUser(ActionEvent event) {
        UserData selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Please select a user to edit.");
            return;
        }
        
        Dialog<UserData> dialog = createUserDialog("Edit User", selectedUser);
        Optional<UserData> result = dialog.showAndWait();
        
        result.ifPresent(user -> {
            try {
                String sql = "UPDATE employee SET username = ?, is_admin = ? WHERE id = ?";
                connect = Database.connectDB();
                prepare = connect.prepareStatement(sql);
                prepare.setString(1, user.getUsername());
                prepare.setBoolean(2, user.getIsAdmin());
                prepare.setInt(3, selectedUser.getId());
                
                int rowsAffected = prepare.executeUpdate();
                if (rowsAffected > 0) {
                    showInfo("User updated successfully!");
                    loadUsers(); // Refresh the table
                } else {
                    showError("Failed to update user.");
                }
                
            } catch (Exception e) {
                showError("Error updating user: " + e.getMessage());
                e.printStackTrace();
            } finally {
                closeDatabaseResources();
            }
        });
    }

    @FXML
    private void backToLanding(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/sample/tusksmochagarden/landing-view.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Tusks Mocha Garden - Admin Dashboard");
            stage.setScene(scene);
            stage.show();

            // Close current window
            ((Stage) userTable.getScene().getWindow()).close();
        } catch (Exception e) {
            showError("Error returning to landing page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Dialog<UserData> createUserDialog(String title, UserData existingUser) {
        Dialog<UserData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        CheckBox adminCheckBox = new CheckBox("Is Admin");

        if (existingUser != null) {
            usernameField.setText(existingUser.getUsername());
            adminCheckBox.setSelected(existingUser.getIsAdmin());
            passwordField.setVisible(false); // Don't show password field for editing
        }

        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(10, 
            new Label("Username:"), usernameField,
            existingUser == null ? new Label("Password:") : new Label(""),
            existingUser == null ? passwordField : new Label(""),
            adminCheckBox));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String password = existingUser == null ? passwordField.getText() : "";
                return new UserData(0, usernameField.getText(), password, adminCheckBox.isSelected());
            }
            return null;
        });

        return dialog;
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback
        }
    }

    private void closeDatabaseResources() {
        try {
            if (result != null) result.close();
            if (prepare != null) prepare.close();
            if (connect != null) connect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (usersErrorLabel != null) {
            usersErrorLabel.setText(message);
        }
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // UserData class for the table
    public static class UserData {
        private int id;
        private String username;
        private String password;
        private boolean isAdmin;

        public UserData(int id, String username, String password, boolean isAdmin) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.isAdmin = isAdmin;
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public boolean getIsAdmin() { return isAdmin; }
        public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
    }
} 