package sample.tusksmochagarden;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;

public class loginController implements Initializable {

    @FXML
    private AnchorPane si_loginForm, su_signupForm, fp_questionForm, np_newPassForm, side_form;

    @FXML
    private TextField si_username, su_username, su_answer, fp_username, fp_answer;

    @FXML
    private PasswordField si_password, su_password, np_newPassword, np_confirmPassword;

    @FXML
    private ComboBox<String> su_question, fp_question, su_admin;

    @FXML
    private Button si_loginBtn, su_signupBtn, fp_proceedBtn, np_changePassBtn, fp_back, np_back, side_CreateBtn, side_alreadyHave;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private final String[] questionList = {"What is your favorite Color?", "What is your favorite food?", "What is your birth date?"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeQuestionLists();
    }

    private void initializeQuestionLists() {
        ObservableList<String> listData = FXCollections.observableArrayList(questionList);
        su_question.setItems(listData);
        fp_question.setItems(listData);
    }

    @FXML
    private void loginBtn() {
        String username = si_username.getText();
        String password = si_password.getText();

        // Bypass the database connection for a test user
        if ("testuser".equals(username) && "testpass".equals(password)) {
            data.username = "testuser";
            showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully Logged In as Test User!");
            try {
                loadMainForm();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Incorrect Username/Password");
            return;
        }

        // Improved security: fetch user by username only, then verify password hash
        String selectData = "SELECT username, password FROM employee WHERE username = ?";
        connect = Database.connectDB();

        try {
            prepare = connect.prepareStatement(selectData);
            prepare.setString(1, username);
            result = prepare.executeQuery();
            
            if (result.next()) {
                String storedHash = result.getString("password");
                // Verify password hash
                if (verifyPassword(password, storedHash)) {
                    data.username = username;
                    showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully Logged In!");
                    loadMainForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error Message", "Incorrect Username/Password");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Incorrect Username/Password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabaseResources();
        }
    }
    
    /**
     * Verify if a password matches the stored hash
     * @param password the plaintext password to check
     * @param storedHash the stored password hash
     * @return true if the password matches, false otherwise
     */
    private boolean verifyPassword(String password, String storedHash) {
        // Generate hash of the provided password
        String passwordHash = hashPassword(password);
        // Compare with stored hash (in a real app, use constant-time comparison)
        return passwordHash.equals(storedHash);
    }
    
    @FXML
    private void switchForgotPass(ActionEvent event) {
        si_loginForm.setVisible(false);
        fp_questionForm.setVisible(true);
    }

    @FXML
    private void regBtn() {
        if (su_username.getText().isEmpty() || su_password.getText().isEmpty() || su_question.getSelectionModel().getSelectedItem() == null || su_answer.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
            return;
        }

        connect = Database.connectDB();

        try {
            if (isUsernameTaken(su_username.getText())) {
                showAlert(Alert.AlertType.ERROR, "Error Message", su_username.getText() + " is already taken");
            } else if (su_password.getText().length() < 8) {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Invalid Password, at least 8 characters are needed");
            } else {
                registerUser();
                showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully registered Account!");
                clearFields(su_username, su_password, su_answer);
                su_question.getSelectionModel().clearSelection();
                switchToLoginForm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabaseResources();
        }
    }

    @FXML
    private void proceedBtn() {
        if (fp_username.getText().isEmpty() || fp_question.getSelectionModel().getSelectedItem() == null || fp_answer.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
            return;
        }

        String selectData = "SELECT username, question, answer FROM employee WHERE username = ? AND question = ? AND answer = ?";
        connect = Database.connectDB();

        try {
            prepare = connect.prepareStatement(selectData);
            prepare.setString(1, fp_username.getText());
            prepare.setString(2, fp_question.getSelectionModel().getSelectedItem());
            prepare.setString(3, fp_answer.getText());
            result = prepare.executeQuery();

            if (result.next()) {
                switchToNewPassForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Incorrect Information");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabaseResources();
        }
    }

    @FXML
    private void changePassBtn() {
        if (np_newPassword.getText().isEmpty() || np_confirmPassword.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
            return;
        }

        if (!np_newPassword.getText().equals(np_confirmPassword.getText())) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Passwords do not match");
            return;
        }

        connect = Database.connectDB();

        try {
            // Use hashed password for security
            String hashedPassword = hashPassword(np_newPassword.getText());
            
            String updatePass = "UPDATE employee SET password = ?, question = ?, answer = ? WHERE username = ?";
            prepare = connect.prepareStatement(updatePass);
            prepare.setString(1, hashedPassword);
            prepare.setString(2, fp_question.getSelectionModel().getSelectedItem());
            prepare.setString(3, fp_answer.getText());
            prepare.setString(4, fp_username.getText());
            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully changed Password!");
            switchToLoginForm();
            clearFields(np_newPassword, np_confirmPassword, fp_username, fp_answer);
            fp_question.getSelectionModel().clearSelection();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabaseResources();
        }
    }

    @FXML
    private void backToLoginForm() {
        si_loginForm.setVisible(true);
        fp_questionForm.setVisible(false);
    }

    @FXML
    private void backToQuestionForm() {
        fp_questionForm.setVisible(true);
        np_newPassForm.setVisible(false);
    }

    @FXML
    private void switchForm(ActionEvent event) {
        TranslateTransition slider = new TranslateTransition(Duration.seconds(.5), side_form);

        if (event.getSource() == side_CreateBtn) {
            slider.setToX(300);
            slider.setOnFinished(e -> showSignUpForm());
        } else if (event.getSource() == side_alreadyHave) {
            slider.setToX(0);
            slider.setOnFinished(e -> showLoginForm());
        }
        slider.play();
    }

    private void showSignUpForm() {
        side_alreadyHave.setVisible(true);
        side_CreateBtn.setVisible(false);
    }

    private void showLoginForm() {
        side_alreadyHave.setVisible(false);
        side_CreateBtn.setVisible(true);
    }

    private void switchToLoginForm() {
        TranslateTransition slider = new TranslateTransition(Duration.seconds(.5), side_form);
        slider.setToX(0);
        slider.setOnFinished(e -> showLoginForm());
        slider.play();
    }

    private void switchToNewPassForm() {
        np_newPassForm.setVisible(true);
        fp_questionForm.setVisible(false);
    }

    private void loadMainForm() throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("mainForm.fxml")));
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setTitle("Tusks Mocha Garden");
        stage.setMinWidth(1100);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        si_loginBtn.getScene().getWindow().hide();
    }

    private void registerUser() throws SQLException {
        String regData = "INSERT INTO employee (username, password, question, answer, hire_date) VALUES(?,?,?,?,?)";
        prepare = connect.prepareStatement(regData);
        prepare.setString(1, su_username.getText());
        
        // Hash the password for security
        String hashedPassword = hashPassword(su_password.getText());
        prepare.setString(2, hashedPassword);
        
        prepare.setString(3, su_question.getSelectionModel().getSelectedItem());
        prepare.setString(4, su_answer.getText());
        
        java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
        prepare.setDate(5, sqlDate);
        prepare.executeUpdate();
    }
    
    /**
     * Hash a password for secure storage
     * @param password the plaintext password to hash
     * @return a secure hash of the password
     */
    private String hashPassword(String password) {
        try {
            // In a real production app, use a proper password hashing library like BCrypt
            // This is a simple implementation using SHA-256 for demonstration
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fall back to plaintext if hashing fails
            return password;
        }
    }

    private boolean isUsernameTaken(String username) throws SQLException {
        String checkUsername = "SELECT username FROM employee WHERE username = ?";
        prepare = connect.prepareStatement(checkUsername);
        prepare.setString(1, username);
        result = prepare.executeQuery();
        return result.next();
    }

    private void closeDatabaseResources() {
        try {
            if (result != null) result.close();
            if (prepare != null) prepare.close();
            if (connect != null) connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }
}