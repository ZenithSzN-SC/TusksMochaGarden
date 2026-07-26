package com.tusksmochagarden.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.tusksmochagarden.model.AppSession;

import java.util.Objects;

public class LandingController {

    @FXML
    private Label landingErrorLabel;

    @FXML
    private void openInventorySystem(ActionEvent event) {
        try {
            // Load the main form (inventory management system)
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/tusksmochagarden/mainForm.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Tusks Mocha Garden - Inventory Management");
            stage.setMinWidth(1100);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.show();

            // Close the landing window
            ((Stage) landingErrorLabel.getScene().getWindow()).close();
        } catch (Exception e) {
            showError("Error opening Inventory Management System: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openUsersSystem(ActionEvent event) {
        try {
            // Load the users view
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/tusksmochagarden/users-view.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Tusks Mocha Garden - User Management");
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            stage.setScene(scene);
            stage.show();

            // Close the landing window
            ((Stage) landingErrorLabel.getScene().getWindow()).close();
        } catch (Exception e) {
            showError("Error opening User Management System: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void signOut(ActionEvent event) {
        try {
            // Clear user data
            AppSession.username = null;
            AppSession.isAdmin = null;
            AppSession.id = null;
            AppSession.cID = null;
            AppSession.path = null;
            AppSession.date = null;

            // Load the login form
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/tusksmochagarden/login.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Tusks Mocha Garden - Login");
            stage.setMinHeight(400);
            stage.setMinWidth(600);
            stage.setScene(scene);
            stage.show();

            // Close the landing window
            ((Stage) landingErrorLabel.getScene().getWindow()).close();
        } catch (Exception e) {
            showError("Error signing out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (landingErrorLabel != null) {
            landingErrorLabel.setText(message);
        }
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 