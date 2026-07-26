package com.tusksmochagarden.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.tusksmochagarden.data.DashboardRepository;
import com.tusksmochagarden.data.OrderRepository;
import com.tusksmochagarden.data.PasswordHasher;
import com.tusksmochagarden.data.ProductRepository;
import com.tusksmochagarden.data.StaffRepository;
import com.tusksmochagarden.model.AppSession;
import com.tusksmochagarden.model.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainFormController implements Initializable {

    // ---------- Views ----------
    @FXML
    private AnchorPane main_form, dashboard_form, staff_form, inventory_form, menu_form, orders_form, settings_form, inventory_editPanel;

    @FXML
    private Pane inventory_scrim;

    @FXML
    private StackPane menu_successPane;

    @FXML
    private VBox dashboard_alertsBox, topSellers_box, staff_rowsBox, orders_listBox, od_linesBox,
            recent_wrap, recent_box, cust_pane, cust_tempWrap, cust_iceWrap, menu_amountRow, menu_walletRow;

    @FXML
    private HBox menu_changeRow, menu_subtotalRow, menu_vatRow;

    // ---------- Labels ----------
    @FXML
    private Label username, sidebar_avatar, sidebar_role,
            dashboard_NC, dashboard_TI, dashboard_TotalI, dashboard_AOV, dashboard_date,
            dashboard_TI_trend, dashboard_NC_trend, dashboard_AOV_trend, dashboard_alertCount,
            inv_totalSKUs, inv_lowStock, inv_outStock, inv_restockMsg, inventory_panelTitle,
            pos_time, pos_date, cust_title, cust_price,
            menu_itemCount, menu_subtotal, menu_vat, menu_total, menu_change,
            od_title, od_meta, od_total, od_paidVia,
            set_avatar, set_name, set_role, set_since, success_msg;

    // ---------- Buttons ----------
    @FXML
    private Button dashboard_btn, staff_btn, inventory_btn, menu_btn, orders_btn, settings_btn, logout_btn;

    @FXML
    private Button inventory_chipAll, inventory_chipBeverage, inventory_chipMeal, inventory_chipDessert,
            inventory_chipOthers, inventory_chipRestock, inventory_clearSearchBtn, inventory_printAllBtn,
            inventory_importBtn, inventory_deleteBtn, inventory_printSingleBtn, inventory_clearBtn, inventory_saveBtn;

    @FXML
    private Button menu_chipAll, menu_chipBeverage, menu_chipMeal, menu_chipDessert, menu_chipOthers,
            menu_takeawayBtn, menu_dineinBtn, menu_cardBtn, menu_cashBtn, menu_walletBtn,
            menu_payBtn, menu_removeBtn, menu_receiptBtn;

    @FXML
    private Button cust_sizeS, cust_sizeM, cust_sizeL, cust_tempHot, cust_tempIced,
            cust_sugar0, cust_sugar30, cust_sugar50, cust_sugar70, cust_sugar100,
            cust_iceNo, cust_iceLess, cust_iceNormal, cust_iceExtra;

    @FXML
    private Button orders_activeBtn, orders_completedBtn, od_advanceBtn,
            tgl_autoPrint, tgl_showVat, tgl_sounds;

    // ---------- Inputs ----------
    @FXML
    private TextField inventory_productID, inventory_productName, inventory_stock, inventory_price,
            inventory_searchField, menu_searchField, menu_amount, cust_note;

    @FXML
    private ComboBox<String> inventory_type, inventory_status;

    // ---------- Tables ----------
    @FXML
    private TableView<Product> inventory_tableView, menu_tableView;

    @FXML
    private TableColumn<Product, String> inventory_col_productID, inventory_col_productName,
            inventory_col_type, inventory_col_stock, inventory_col_price, inventory_col_status, inventory_col_date;

    @FXML
    private TableColumn<Product, String> menu_col_productName;

    @FXML
    private TableColumn<Product, Integer> menu_col_quantity;

    @FXML
    private TableColumn<Product, Double> menu_col_price;

    @FXML
    private ImageView inventory_imageView;

    @FXML
    private AreaChart<String, Number> dashboard_incomeChart;

    @FXML
    private BarChart<String, Number> dashboard_CustomerChart;

    // ---------- State ----------
    private final ProductRepository productRepository = new ProductRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final StaffRepository staffRepository = new StaffRepository();
    private final DashboardRepository dashboardRepository = new DashboardRepository();

    private ObservableList<Product> cardListData = FXCollections.observableArrayList();
    private ObservableList<Product> inventoryListData;
    private ObservableList<Product> menuOrderListData;

    private double totalP;
    private int cID;

    private static final int LOW_STOCK_THRESHOLD = 10;
    private String inventoryTypeFilter = "All";
    private boolean inventoryRestockOnly = false;
    private boolean inventoryEditMode = false;
    private String menuTypeFilter = "All";
    private String orderTypeSel = "Takeaway";
    private String payMethodSel = "Card";

    // Drink customization
    private Product custProduct;
    private String custSize = "M", custTemp = "Hot", custSugar = "50%", custIce = "Normal";

    // Orders view
    private String ordersTab = "Active";
    private int selectedOrderId = -1;

    private String lastReceiptContent = "";
    private int lastOrderId = -1;

    // =====================================================================
    // Dashboard
    // =====================================================================

    public void dashboardDisplayNC() {
        java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
        dashboard_NC.setText(String.valueOf(dashboardRepository.newCustomersToday(sqlDate)));
    }

    public void dashboardDisplayTI() {
        java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
        dashboard_TI.setText(String.format("£%.2f", dashboardRepository.totalIncomeForDate(sqlDate)));
    }

    public void dashboardTotalI() {
        dashboard_TotalI.setText(String.format("£%.2f", dashboardRepository.totalIncomeAllTime()));
    }

    public void dashboardDisplayAOV() {
        java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
        dashboard_AOV.setText(String.format("£%.2f", dashboardRepository.averageOrderValueForDate(sqlDate)));
    }

    public void dashboardDate() {
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("EEEE, MMM d");
        dashboard_date.setText(fmt.format(new Date()));
    }

    public void dashboardTrends() {
        java.sql.Date today = new java.sql.Date(new Date().getTime());
        java.sql.Date yesterday = java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(1));
        double revToday = dashboardRepository.receiptSumForDate(today), revYesterday = dashboardRepository.receiptSumForDate(yesterday);
        int ordToday = dashboardRepository.receiptCountForDate(today), ordYesterday = dashboardRepository.receiptCountForDate(yesterday);
        double aovToday = ordToday > 0 ? revToday / ordToday : 0;
        double aovYesterday = ordYesterday > 0 ? revYesterday / ordYesterday : 0;
        setTrend(dashboard_TI_trend, revToday, revYesterday);
        setTrend(dashboard_NC_trend, ordToday, ordYesterday);
        setTrend(dashboard_AOV_trend, aovToday, aovYesterday);
    }

    private void setTrend(Label label, double today, double yesterday) {
        if (yesterday <= 0) {
            label.setVisible(false);
            return;
        }
        double pct = (today - yesterday) / yesterday * 100.0;
        label.setVisible(true);
        label.setText((pct >= 0 ? "▲ " : "▼ ") + String.format("%.1f%%", Math.abs(pct)));
        label.getStyleClass().removeAll("trend-up", "trend-down");
        label.getStyleClass().add(pct >= 0 ? "trend-up" : "trend-down");
    }

    public void dashboardAlerts() {
        dashboard_alertsBox.getChildren().clear();
        int count = 0;
        for (DashboardRepository.LowStockAlert a : dashboardRepository.lowStockAlerts(LOW_STOCK_THRESHOLD)) {
            count++;
            Label name = new Label(a.productName());
            name.getStyleClass().add("alert-name");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            int stock = a.stock();
            Label badge = new Label(stock == 0 ? "Out of stock" : stock + " left");
            badge.getStyleClass().add("alert-badge");
            HBox row = new HBox(8, name, spacer, badge);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("alert-row");
            dashboard_alertsBox.getChildren().add(row);
        }
        dashboard_alertCount.setText(String.valueOf(count));
        if (count == 0) {
            Label ok = new Label("All stocked up — nothing needs attention.");
            ok.getStyleClass().add("subtitle");
            ok.setWrapText(true);
            dashboard_alertsBox.getChildren().add(ok);
        }
    }

    public void dashboardTopSellers() {
        topSellers_box.getChildren().clear();
        int rank = 0;
        for (DashboardRepository.TopSeller seller : dashboardRepository.topSellersToday()) {
            rank++;
            Label rankLbl = new Label(String.valueOf(rank));
            rankLbl.getStyleClass().add("rank-circle");
            rankLbl.setMinSize(24, 24);
            rankLbl.setPrefSize(24, 24);
            Label name = new Label(seller.productName());
            name.getStyleClass().add("card-title");
            name.setStyle("-fx-font-size: 13px;");
            Label sold = new Label(seller.quantitySold() + " sold today");
            sold.getStyleClass().add("subtitle");
            sold.setStyle("-fx-font-size: 11px;");
            VBox info = new VBox(1, name, sold);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label rev = new Label(String.format("£%.2f", seller.revenue()));
            rev.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #C4836A;");
            HBox row = new HBox(10, rankLbl, info, spacer, rev);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("seller-row");
            row.setPadding(new Insets(6, 8, 6, 8));
            topSellers_box.getChildren().add(row);
        }
        if (rank == 0) {
            Label none = new Label("No sales yet today.");
            none.getStyleClass().add("subtitle");
            topSellers_box.getChildren().add(none);
        }
    }

    public void dashboardIncomeChart() {
        dashboard_incomeChart.getData().clear();
        XYChart.Series<String, Number> chart = new XYChart.Series<>();
        for (DashboardRepository.ChartPoint p : dashboardRepository.incomeLast7Days()) {
            chart.getData().add(new XYChart.Data<>(p.label(), p.value()));
        }
        dashboard_incomeChart.getData().add(chart);
    }

    public void dashboardCustomerChart() {
        dashboard_CustomerChart.getData().clear();
        XYChart.Series<String, Number> chart = new XYChart.Series<>();
        for (DashboardRepository.ChartPoint p : dashboardRepository.ordersLast7Days()) {
            chart.getData().add(new XYChart.Data<>(p.label(), p.value()));
        }
        dashboard_CustomerChart.getData().add(chart);
    }

    private void loadDashboardData() {
        try {
            dashboardDisplayNC();
            dashboardDisplayTI();
            dashboardTotalI();
            dashboardDisplayAOV();
            dashboardDate();
            dashboardTrends();
            dashboardAlerts();
            dashboardTopSellers();
            dashboardIncomeChart();
            dashboardCustomerChart();
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // Staff Management
    // =====================================================================

    public void staffShowData() {
        staff_rowsBox.getChildren().clear();
        try {
            for (StaffRepository.StaffRow row : staffRepository.listStaff()) {
                staff_rowsBox.getChildren().add(buildStaffRow(row.username(), row.isAdmin(), row.lastActive()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buildStaffRow(String name, boolean admin, java.sql.Timestamp lastActive) {
        Label avatar = new Label(initialsOf(name));
        avatar.getStyleClass().add("avatar");
        avatar.setMinSize(36, 36);
        avatar.setPrefSize(36, 36);
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("card-title");
        nameLbl.setStyle("-fx-font-size: 14px;");
        HBox member = new HBox(12, avatar, nameLbl);
        member.setAlignment(Pos.CENTER_LEFT);
        member.setPrefWidth(260);

        Label role = new Label(admin ? "Admin" : "Barista");
        role.getStyleClass().add(admin ? "badge-admin" : "badge-barista");
        HBox roleBox = new HBox(role);
        roleBox.setAlignment(Pos.CENTER_LEFT);
        roleBox.setPrefWidth(120);

        boolean active = lastActive != null
                && System.currentTimeMillis() - lastActive.getTime() < 15 * 60 * 1000;
        Region dot = new Region();
        dot.setMinSize(8, 8);
        dot.setPrefSize(8, 8);
        dot.setMaxSize(8, 8);
        dot.getStyleClass().add(active ? "dot-active" : "dot-offline");
        Label statusLbl = new Label(active ? "Active" : "Offline");
        statusLbl.setStyle(active
                ? "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2A2422;"
                : "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #A09693;");
        HBox statusBox = new HBox(8, dot, statusLbl);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPrefWidth(130);

        Label lastLbl = new Label(relativeTime(lastActive));
        lastLbl.getStyleClass().add("subtitle");
        HBox lastBox = new HBox(lastLbl);
        lastBox.setAlignment(Pos.CENTER_LEFT);
        lastBox.setPrefWidth(160);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button edit = new Button("✎");
        edit.getStyleClass().add("icon-btn");
        edit.setOnAction(e -> staffEditDialog(name, admin));

        HBox row = new HBox(member, roleBox, statusBox, lastBox, spacer, edit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("staff-row");
        row.setPadding(new Insets(10, 22, 10, 22));
        return row;
    }

    private String initialsOf(String name) {
        if (name == null || name.isEmpty()) return "??";
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String relativeTime(java.sql.Timestamp ts) {
        if (ts == null) return "Never";
        long mins = (System.currentTimeMillis() - ts.getTime()) / 60000;
        if (mins < 2) return "Just now";
        if (mins < 60) return mins + " minutes ago";
        long hours = mins / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        return days + (days == 1 ? " day ago" : " days ago");
    }

    @FXML
    public void staffAddMember() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Member");
        dialog.setHeaderText("Create a staff account");
        TextField user = new TextField();
        user.setPromptText("Username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password (min 8 chars)");
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Barista", "Admin"));
        role.getSelectionModel().selectFirst();
        ComboBox<String> question = new ComboBox<>(FXCollections.observableArrayList(
                "What is your favorite Color?", "What is your favorite food?", "What is your birth date?"));
        question.getSelectionModel().selectFirst();
        TextField answer = new TextField();
        answer.setPromptText("Security answer");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Username"), user);
        grid.addRow(1, new Label("Password"), pass);
        grid.addRow(2, new Label("Role"), role);
        grid.addRow(3, new Label("Question"), question);
        grid.addRow(4, new Label("Answer"), answer);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;
        if (user.getText().isEmpty() || pass.getText().length() < 8 || answer.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Username, an 8+ character password, and an answer are required.");
            return;
        }
        try {
            if (staffRepository.isUsernameTaken(user.getText())) {
                showAlert(Alert.AlertType.ERROR, user.getText() + " is already taken");
                return;
            }
            staffRepository.addStaff(user.getText(), PasswordHasher.hash(pass.getText()),
                    question.getValue(), answer.getText(), "Admin".equals(role.getValue()));
            showAlert(Alert.AlertType.INFORMATION, "Member added!");
            staffShowData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void staffEditDialog(String name, boolean admin) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Member");
        dialog.setHeaderText("Editing " + name);
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Barista", "Admin"));
        role.getSelectionModel().select(admin ? "Admin" : "Barista");
        PasswordField pass = new PasswordField();
        pass.setPromptText("New password (leave blank to keep)");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Role"), role);
        grid.addRow(1, new Label("Password"), pass);
        dialog.getDialogPane().setContent(grid);
        ButtonType removeType = new ButtonType("Remove Member", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(removeType, ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty()) return;
        try {
            if (res.get() == removeType) {
                if (name.equals(AppSession.username)) {
                    showAlert(Alert.AlertType.ERROR, "You can't remove the account you're signed in with.");
                    return;
                }
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove " + name + "?");
                Optional<ButtonType> ok = confirm.showAndWait();
                if (ok.isPresent() && ok.get() == ButtonType.OK) {
                    staffRepository.removeStaff(name);
                    staffShowData();
                }
            } else if (res.get() == ButtonType.OK) {
                if (!pass.getText().isEmpty() && pass.getText().length() < 8) {
                    showAlert(Alert.AlertType.ERROR, "Password must be at least 8 characters.");
                    return;
                }
                boolean isAdmin = "Admin".equals(role.getValue());
                if (pass.getText().isEmpty()) {
                    staffRepository.updateRole(name, isAdmin);
                } else {
                    staffRepository.updateRoleAndPassword(name, isAdmin, PasswordHasher.hash(pass.getText()));
                }
                staffShowData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // Inventory
    // =====================================================================

    public boolean inventoryAddBtn() {
        boolean saved = false;

        if (!validateInventoryForm()) {
            return false;
        }

        try {
            Integer.parseInt(inventory_stock.getText());
            Double.parseDouble(inventory_price.getText().replace("£", ""));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Stock must be a whole number and price must be a valid number");
            return false;
        }

        try {
            if (productRepository.existsById(inventory_productID.getText())) {
                showAlert(Alert.AlertType.ERROR, inventory_productID.getText() + " is already taken");
            } else {
                int stock = Integer.parseInt(inventory_stock.getText());
                if (stock < 0) {
                    showAlert(Alert.AlertType.ERROR, "Stock cannot be negative");
                    return false;
                }

                double price = Double.parseDouble(inventory_price.getText().replace("£", ""));
                if (price <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Price must be greater than zero");
                    return false;
                }

                String path = AppSession.path;
                if (path != null) {
                    path = path.replace("\\", "\\\\");
                }

                productRepository.insert(
                        inventory_productID.getText().trim(),
                        inventory_productName.getText().trim(),
                        inventory_type.getSelectionModel().getSelectedItem(),
                        stock, price,
                        inventory_status.getSelectionModel().getSelectedItem(),
                        path,
                        new java.sql.Date(new Date().getTime()));

                showAlert(Alert.AlertType.INFORMATION, "Product added successfully!");
                inventoryShowData();
                inventoryClearBtn();
                saved = true;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return saved;
    }

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

        if (!inventory_productID.getText().matches("[a-zA-Z0-9]+")) {
            showAlert(Alert.AlertType.ERROR, "Product ID must contain only letters and numbers");
            return false;
        }

        if (inventory_productName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Product name cannot be empty");
            return false;
        }

        if (inventory_imageView.getImage() == null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("No Image Selected");
            alert.setHeaderText(null);
            alert.setContentText("You haven't selected a product image. Do you want to continue without an image?");
            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.CANCEL) {
                return false;
            }
        }

        return true;
    }

    public boolean inventoryUpdateBtn() {
        boolean saved = false;

        if (inventory_productID.getText().isEmpty() ||
            inventory_productName.getText().isEmpty() ||
            inventory_type.getSelectionModel().getSelectedItem() == null ||
            inventory_stock.getText().isEmpty() ||
            inventory_price.getText().isEmpty() ||
            inventory_status.getSelectionModel().getSelectedItem() == null) {

            showAlert(Alert.AlertType.ERROR, "Please select an item first");
            return false;
        }

        String path = AppSession.path;
        if (path != null) {
            path = path.replace("\\", "\\\\");
        }

        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to UPDATE Product ID: " + inventory_productID.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {
                productRepository.update(
                        inventory_productID.getText(),
                        inventory_productName.getText(),
                        inventory_type.getSelectionModel().getSelectedItem(),
                        Integer.parseInt(inventory_stock.getText()),
                        Double.parseDouble(inventory_price.getText().replace("£", "")),
                        inventory_status.getSelectionModel().getSelectedItem(),
                        path,
                        new java.sql.Date(new Date().getTime()));

                showAlert(Alert.AlertType.INFORMATION, "Successfully Updated!");
                inventoryShowData();
                inventoryClearBtn();
                saved = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saved;
    }

    @FXML
    public void inventoryDeleteBtn() {
        if (inventory_productID.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select an item first");
            return;
        }

        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to DELETE Product ID: " + inventory_productID.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {
                productRepository.delete(inventory_productID.getText());

                showAlert(Alert.AlertType.INFORMATION, "Successfully Deleted!");
                inventoryShowData();
                inventoryClearBtn();
                inventoryClosePanel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void inventoryClearBtn() {
        inventory_productID.clear();
        inventory_productID.setDisable(false);
        inventory_productName.clear();
        inventory_type.getSelectionModel().clearSelection();
        inventory_stock.clear();
        inventory_price.clear();
        inventory_status.getSelectionModel().clearSelection();
        inventory_imageView.setImage(null);
    }

    @FXML
    public void inventoryOpenAdd() {
        inventoryClearBtn();
        inventoryEditMode = false;
        inventory_panelTitle.setText("Add New Product");
        openInventoryPanel();
    }

    private void openInventoryPanel() {
        inventory_scrim.setVisible(true);
        TranslateTransition slide = new TranslateTransition(Duration.millis(260), inventory_editPanel);
        slide.setToX(0);
        slide.play();
    }

    @FXML
    public void inventoryClosePanel() {
        inventory_scrim.setVisible(false);
        TranslateTransition slide = new TranslateTransition(Duration.millis(260), inventory_editPanel);
        slide.setToX(480);
        slide.play();
    }

    @FXML
    public void inventorySaveBtn() {
        boolean saved = inventoryEditMode ? inventoryUpdateBtn() : inventoryAddBtn();
        if (saved) {
            inventoryClosePanel();
        }
    }

    @FXML
    public void inventoryChipFilter(ActionEvent event) {
        Object source = event.getSource();
        if (source == inventory_chipRestock) {
            inventoryRestockOnly = !inventoryRestockOnly;
            inventory_chipRestock.getStyleClass().remove("chip-restock-active");
            if (inventoryRestockOnly) {
                inventory_chipRestock.getStyleClass().add("chip-restock-active");
            }
        } else {
            inventoryTypeFilter = ((Button) source).getText();
            Button[] chips = {inventory_chipAll, inventory_chipBeverage, inventory_chipMeal, inventory_chipDessert, inventory_chipOthers};
            for (Button chip : chips) {
                chip.getStyleClass().remove("chip-active");
                if (chip == source) {
                    chip.getStyleClass().add("chip-active");
                }
            }
        }
        applyInventoryFilters();
    }

    private boolean isRestockNeeded(Product p) {
        return p.getStock() <= LOW_STOCK_THRESHOLD || "Unavailable".equals(p.getStatus());
    }

    public void applyInventoryFilters() {
        if (inventoryListData == null) {
            return;
        }
        String search = inventory_searchField.getText() == null ? "" : inventory_searchField.getText().toLowerCase();
        ObservableList<Product> filtered = FXCollections.observableArrayList();
        for (Product p : inventoryListData) {
            if (!"All".equals(inventoryTypeFilter) && !inventoryTypeFilter.equals(p.getType())) continue;
            if (inventoryRestockOnly && !isRestockNeeded(p)) continue;
            if (!search.isEmpty()
                    && !p.getProductName().toLowerCase().contains(search)
                    && !p.getProductId().toLowerCase().contains(search)
                    && !p.getType().toLowerCase().contains(search)) continue;
            filtered.add(p);
        }
        inventory_tableView.setItems(filtered);
    }

    public void inventorySummary() {
        if (inventoryListData == null) {
            return;
        }
        int low = 0, out = 0;
        StringBuilder issues = new StringBuilder();
        for (Product p : inventoryListData) {
            boolean isOut = p.getStock() == 0 || "Unavailable".equals(p.getStatus());
            boolean isLow = !isOut && p.getStock() <= LOW_STOCK_THRESHOLD;
            if (isOut) out++;
            if (isLow) low++;
            if (isOut || isLow) {
                if (issues.length() > 0) issues.append("  ·  ");
                issues.append(p.getProductName()).append(isOut ? " is out of stock" : " is running low");
            }
        }
        inv_totalSKUs.setText(String.valueOf(inventoryListData.size()));
        inv_lowStock.setText(String.valueOf(low));
        inv_outStock.setText(String.valueOf(out));
        inv_restockMsg.setText(issues.length() == 0 ? "All items are healthy." : issues.toString());
    }

    private void inventoryStatusBadges() {
        inventory_col_status.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Product p = getTableRow().getItem();
                Label badge = new Label();
                badge.getStyleClass().add("status-badge");
                if (p.getStock() == 0 || "Unavailable".equals(p.getStatus())) {
                    badge.setText("Out of Stock");
                    badge.getStyleClass().add("badge-red");
                } else if (p.getStock() <= LOW_STOCK_THRESHOLD) {
                    badge.setText("Low Stock");
                    badge.getStyleClass().add("badge-orange");
                } else {
                    badge.setText("Available");
                    badge.getStyleClass().add("badge-green");
                }
                setGraphic(badge);
            }
        });
    }

    @FXML
    public void inventoryImportBtn() {
        FileChooser openFile = new FileChooser();
        openFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Open Image File", "*png", "*jpg"));
        File file = openFile.showOpenDialog(main_form.getScene().getWindow());
        if (file != null) {
            AppSession.path = file.getAbsolutePath();
            var image = new Image(file.toURI().toString(), 148, 108, true, true);
            inventory_imageView.setImage(image);
        }
    }

    @FXML
    public void inventorySelectData() {
        Product prodData = inventory_tableView.getSelectionModel().getSelectedItem();
        int num = inventory_tableView.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1 || prodData == null) {
            return;
        }

        inventory_productID.setText(prodData.getProductId());
        inventory_productName.setText(prodData.getProductName());
        inventory_stock.setText(String.valueOf(prodData.getStock()));
        inventory_price.setText(String.valueOf(prodData.getPrice()));
        inventory_type.getSelectionModel().select(prodData.getType());
        inventory_status.getSelectionModel().select(prodData.getStatus());

        String path = "file:" + prodData.getImage();
        AppSession.path = prodData.getImage();

        if (prodData.getImage() != null && !prodData.getImage().isEmpty()) {
            Image image = new Image(path, 148, 108, true, true);
            inventory_imageView.setImage(image);
        }

        inventoryEditMode = true;
        inventory_panelTitle.setText("Edit Product");
        inventory_productID.setDisable(true);
        openInventoryPanel();
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
        inventoryStatusBadges();
        applyInventoryFilters();
        inventorySummary();
    }

    public ObservableList<Product> inventoryDataList() {
        return productRepository.findAllForInventory();
    }

    @FXML
    public void inventorySearchProduct() {
        applyInventoryFilters();
    }

    @FXML
    public void inventoryClearSearch() {
        inventory_searchField.clear();
        applyInventoryFilters();
    }

    // =====================================================================
    // Register (POS)
    // =====================================================================

    @FXML
    public void menuChipFilter(ActionEvent event) {
        menuTypeFilter = ((Button) event.getSource()).getText();
        Button[] chips = {menu_chipAll, menu_chipBeverage, menu_chipMeal, menu_chipDessert, menu_chipOthers};
        for (Button chip : chips) {
            chip.getStyleClass().remove("chip-active");
            if (chip == event.getSource()) {
                chip.getStyleClass().add("chip-active");
            }
        }
        menuDisplayCard();
    }

    @FXML
    public void menuSearch() {
        menuDisplayCard();
    }

    public void menuDisplayCard() {
        cardListData.clear();
        cardListData.addAll(menuGetData());
        menu_gridPane.getChildren().clear();
        String search = (menu_searchField == null || menu_searchField.getText() == null) ? "" : menu_searchField.getText().toLowerCase();
        int row = 0, column = 0;
        for (Product product : cardListData) {
            if (!"All".equals(menuTypeFilter) && !menuTypeFilter.equals(product.getType())) continue;
            if (!search.isEmpty() && !product.getProductName().toLowerCase().contains(search)) continue;
            try {
                FXMLLoader load = new FXMLLoader();
                load.setLocation(getClass().getResource("/com/tusksmochagarden/cardProduct.fxml"));
                AnchorPane pane = load.load();

                CardProductController cardC = load.getController();
                cardC.setData(product);
                cardC.setMainFormController(this);

                if (column == 3) {
                    column = 0;
                    row++;
                }
                menu_gridPane.add(pane, column++, row);
                GridPane.setMargin(pane, new Insets(8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private GridPane menu_gridPane;

    @FXML
    private ScrollPane menu_scrollPane;

    public ObservableList<Product> menuGetData() {
        return productRepository.findAllForMenu();
    }

    /** Entry point for card taps: drinks open the customization panel, everything else adds directly. */
    public void menuItemTapped(Product product) {
        if ("Beverage".equals(product.getType())) {
            openCustomize(product);
        } else {
            addToOrder(product, 1, "", 0);
        }
    }

    // ---------- Drink customization ----------

    private void openCustomize(Product product) {
        custProduct = product;
        custSize = "M";
        custSugar = "50%";
        custIce = "Normal";
        cust_note.clear();
        String name = product.getProductName().toLowerCase();
        custTemp = name.contains("iced") ? "Iced" : "Hot";
        boolean isEspresso = name.contains("espresso");
        cust_tempWrap.setVisible(!isEspresso);
        cust_title.setText("Customize " + product.getProductName());
        syncCustStyles();
        updateCustPrice();
        cust_pane.setVisible(true);
    }

    @FXML
    public void custBack() {
        cust_pane.setVisible(false);
    }

    @FXML
    public void custSize(ActionEvent e) {
        if (e.getSource() == cust_sizeS) custSize = "S";
        else if (e.getSource() == cust_sizeL) custSize = "L";
        else custSize = "M";
        syncCustStyles();
        updateCustPrice();
    }

    @FXML
    public void custTemp(ActionEvent e) {
        custTemp = e.getSource() == cust_tempIced ? "Iced" : "Hot";
        syncCustStyles();
    }

    @FXML
    public void custSugar(ActionEvent e) {
        custSugar = ((Button) e.getSource()).getText();
        syncCustStyles();
    }

    @FXML
    public void custIce(ActionEvent e) {
        custIce = ((Button) e.getSource()).getText();
        syncCustStyles();
    }

    private void syncCustStyles() {
        setOptActive(new Button[]{cust_sizeS, cust_sizeM, cust_sizeL},
                "S".equals(custSize) ? cust_sizeS : "L".equals(custSize) ? cust_sizeL : cust_sizeM, "opt-active");
        setOptActive(new Button[]{cust_tempHot, cust_tempIced},
                "Iced".equals(custTemp) ? cust_tempIced : cust_tempHot,
                "Iced".equals(custTemp) ? "opt-active-cool" : "opt-active-hot");
        Button sugarBtn = switch (custSugar) {
            case "0%" -> cust_sugar0;
            case "30%" -> cust_sugar30;
            case "70%" -> cust_sugar70;
            case "100%" -> cust_sugar100;
            default -> cust_sugar50;
        };
        setOptActive(new Button[]{cust_sugar0, cust_sugar30, cust_sugar50, cust_sugar70, cust_sugar100}, sugarBtn, "opt-active");
        Button iceBtn = switch (custIce) {
            case "No Ice" -> cust_iceNo;
            case "Less" -> cust_iceLess;
            case "Extra" -> cust_iceExtra;
            default -> cust_iceNormal;
        };
        setOptActive(new Button[]{cust_iceNo, cust_iceLess, cust_iceNormal, cust_iceExtra}, iceBtn, "opt-active-cool");
        cust_iceWrap.setVisible("Iced".equals(custTemp));
    }

    private void setOptActive(Button[] group, Button active, String activeClass) {
        for (Button b : group) {
            b.getStyleClass().removeAll("opt-active", "opt-active-cool", "opt-active-hot");
            if (b == active) {
                b.getStyleClass().add(activeClass);
            }
        }
    }

    private double custSizeDelta() {
        return "S".equals(custSize) ? -0.40 : "L".equals(custSize) ? 0.60 : 0.0;
    }

    private void updateCustPrice() {
        if (custProduct != null) {
            cust_price.setText(String.format("£%.2f", custProduct.getPrice() + custSizeDelta()));
        }
    }

    @FXML
    public void custAdd() {
        if (custProduct == null) return;
        StringBuilder opts = new StringBuilder();
        opts.append("S".equals(custSize) ? "Small" : "L".equals(custSize) ? "Large" : "Regular");
        if (cust_tempWrap.isVisible()) {
            opts.append(" · ").append(custTemp);
        }
        opts.append(" · ").append(custSugar).append(" sugar");
        if ("Iced".equals(custTemp) && !"Normal".equals(custIce)) {
            opts.append(" · ").append(custIce).append(" ice");
        }
        if (!cust_note.getText().isEmpty()) {
            opts.append(" · \"").append(cust_note.getText().trim()).append("\"");
        }
        addToOrder(custProduct, 1, opts.toString(), custSizeDelta());
        cust_pane.setVisible(false);
    }

    // ---------- Cart ----------

    /** Adds qty of a product to the current order, merging identical lines (same product + options). */
    public void addToOrder(Product product, int qty, String options, double priceDelta) {
        customerID();
        try {
            ProductRepository.StockAndStatus stockAndStatus = productRepository.stockAndStatus(product.getProductId());
            int stock = stockAndStatus.stock();
            String status = stockAndStatus.status();

            if (!"Available".equals(status) || stock <= 0) {
                showAlert(Alert.AlertType.ERROR, product.getProductName() + " is not available right now.");
                return;
            }
            if (stock < qty) {
                showAlert(Alert.AlertType.ERROR, "Not enough stock for " + product.getProductName() + ".");
                return;
            }

            double unit = product.getPrice() + priceDelta;
            double lineTotal = unit * qty;

            // merge with an identical line if present
            Optional<OrderRepository.CartLine> existing =
                    orderRepository.findExistingCartLine(AppSession.cID, product.getProductId(), options, unit);

            if (existing.isPresent()) {
                orderRepository.mergeCartLine(existing.get().lineId(), qty, lineTotal);
            } else {
                orderRepository.insertCartLine(AppSession.cID, product.getProductId(), product.getProductName(),
                        product.getType(), qty, lineTotal, new java.sql.Date(new Date().getTime()),
                        product.getImage(), AppSession.username, options);
            }

            adjustStock(product.getProductId(), -qty);

            menuShowOrderData();
            menuGetTotal();
            menuDisplayCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adjustStock(String prodId, int delta) throws SQLException {
        productRepository.adjustStock(prodId, delta);
    }

    /** Cart line +/- stepper; removes the line at zero and restores stock. */
    private void adjustCartLine(Product line, int delta) {
        try {
            double unit = line.getQuantity() > 0 ? line.getPrice() / line.getQuantity() : line.getPrice();

            if (delta > 0) {
                int stock = productRepository.currentStock(line.getProductId());
                if (stock < delta) {
                    showAlert(Alert.AlertType.ERROR, "No more stock for " + line.getProductName() + ".");
                    return;
                }
            }

            int newQty = line.getQuantity() + delta;
            if (newQty <= 0) {
                orderRepository.deleteCartLine(line.getId());
            } else {
                orderRepository.updateCartLine(line.getId(), newQty, unit * newQty);
            }

            adjustStock(line.getProductId(), -delta);

            menuShowOrderData();
            menuGetTotal();
            menuDisplayCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void menuRemoveBtn() {
        if (menuOrderListData == null || menuOrderListData.isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Order");
        alert.setHeaderText(null);
        alert.setContentText("Clear the whole order?");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.isEmpty() || option.get() != ButtonType.OK) return;

        try {
            for (Product line : menuOrderListData) {
                adjustStock(line.getProductId(), line.getQuantity());
            }
            orderRepository.clearCart(AppSession.cID);
            menuShowOrderData();
            menuGetTotal();
            menuDisplayCard();
            resetPaymentFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void menuShowOrderData() {
        menuOrderListData = menuGetOrderList();

        menu_col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        menu_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        menu_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Item cell shows the name plus its customization line
        menu_col_productName.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Product line = getTableRow().getItem();
                Label name = new Label(line.getProductName());
                name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2A2422;");
                VBox box = new VBox(1, name);
                String opts = line.getType(); // cart rows carry their options string in the type slot
                if (opts != null && !opts.isEmpty()) {
                    Label optLbl = new Label(opts);
                    optLbl.getStyleClass().add("cart-opts");
                    optLbl.setWrapText(true);
                    box.getChildren().add(optLbl);
                }
                setGraphic(box);
            }
        });

        // Qty cell becomes a stepper
        menu_col_quantity.setCellFactory(col -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Product line = getTableRow().getItem();
                Button minus = new Button(line.getQuantity() == 1 ? "🗑" : "−");
                minus.getStyleClass().add("qty-btn");
                minus.setOnAction(e -> adjustCartLine(line, -1));
                Label qty = new Label(String.valueOf(line.getQuantity()));
                qty.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2A2422;");
                Button plus = new Button("+");
                plus.getStyleClass().addAll("qty-btn", "qty-plus");
                plus.setOnAction(e -> adjustCartLine(line, 1));
                HBox box = new HBox(6, minus, qty, plus);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        menu_tableView.setItems(menuOrderListData);

        int items = 0;
        for (Product p : menuOrderListData) {
            items += p.getQuantity();
        }
        menu_itemCount.setText(items + (items == 1 ? " item" : " items"));
    }

    private ObservableList<Product> menuGetOrderList() {
        customerID();
        return orderRepository.cartLines(AppSession.cID);
    }

    public void menuGetTotal() {
        customerID();
        totalP = orderRepository.cartTotal(AppSession.cID);

        // VAT-inclusive split in integer pence so the lines always add up
        long totalPence = Math.round(totalP * 100);
        long subtotalPence = Math.round(totalPence / 1.2);
        long vatPence = totalPence - subtotalPence;
        menu_subtotal.setText(String.format("£%.2f", subtotalPence / 100.0));
        menu_vat.setText(String.format("£%.2f", vatPence / 100.0));
        menu_total.setText(String.format("£%.2f", totalP));
        menu_payBtn.setText(String.format("Charge £%.2f", totalP));
        updatePayButtonState();
    }

    public void customerID() {
        cID = orderRepository.nextCustomerId();
        AppSession.cID = cID;
    }

    // ---------- Order type & payment ----------

    @FXML
    public void menuOrderType(ActionEvent event) {
        orderTypeSel = event.getSource() == menu_dineinBtn ? "Dine-in" : "Takeaway";
        menu_takeawayBtn.getStyleClass().remove("seg-active");
        menu_dineinBtn.getStyleClass().remove("seg-active");
        ((Button) event.getSource()).getStyleClass().add("seg-active");
    }

    @FXML
    public void menuPayMethod(ActionEvent event) {
        if (event.getSource() == menu_cashBtn) payMethodSel = "Cash";
        else if (event.getSource() == menu_walletBtn) payMethodSel = "Wallet";
        else payMethodSel = "Card";
        menu_cardBtn.getStyleClass().remove("seg-active");
        menu_cashBtn.getStyleClass().remove("seg-active");
        menu_walletBtn.getStyleClass().remove("seg-active");
        ((Button) event.getSource()).getStyleClass().add("seg-active");
        boolean cash = "Cash".equals(payMethodSel);
        menu_amountRow.setVisible(cash);
        menu_changeRow.setVisible(cash);
        menu_walletRow.setVisible("Wallet".equals(payMethodSel));
        updatePayButtonState();
    }

    private void updatePayButtonState() {
        if ("Cash".equals(payMethodSel)) {
            recomputeCashChange();
        } else {
            menu_payBtn.setDisable(totalP <= 0);
        }
    }

    @FXML
    public void numpadPress(ActionEvent event) {
        String key = ((Button) event.getSource()).getText();
        String cur = menu_amount.getText() == null ? "" : menu_amount.getText();
        if ("C".equals(key)) {
            menu_amount.clear();
        } else if (".".equals(key)) {
            if (!cur.contains(".")) menu_amount.setText(cur.isEmpty() ? "0." : cur + ".");
        } else {
            menu_amount.setText(cur + key);
        }
        recomputeCashChange();
    }

    @FXML
    public void quickAmount(ActionEvent event) {
        String text = ((Button) event.getSource()).getText();
        if ("Exact".equals(text)) {
            menu_amount.setText(String.format("%.2f", totalP));
        } else {
            menu_amount.setText(text.replace("£", "") + ".00");
        }
        recomputeCashChange();
    }

    @FXML
    public void menuAmountTyped() {
        recomputeCashChange();
    }

    private void recomputeCashChange() {
        String raw = menu_amount.getText() == null ? "" : menu_amount.getText().replace("£", "").trim();
        double amount = -1;
        if (raw.matches("\\d+(\\.\\d{0,2})?")) {
            amount = Double.parseDouble(raw);
        }
        if (amount >= totalP && totalP > 0) {
            menu_change.setText(String.format("£%.2f", amount - totalP));
            menu_payBtn.setDisable(false);
        } else {
            menu_change.setText("£0.00");
            menu_payBtn.setDisable(true);
        }
    }

    public void menuAmount(ActionEvent actionEvent) {
        recomputeCashChange();
        if (menu_payBtn.isDisabled() && totalP > 0) {
            showAlert(Alert.AlertType.ERROR,
                    String.format("Payment amount must cover the total £%.2f", totalP));
        }
    }

    // ---------- Charge ----------

    @FXML
    public void menuPayBtn() {
        if (menuOrderListData == null || menuOrderListData.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please order first!");
            return;
        }

        double amount;
        if ("Cash".equals(payMethodSel)) {
            String raw = menu_amount.getText() == null ? "" : menu_amount.getText().replace("£", "").trim();
            if (!raw.matches("\\d+(\\.\\d{0,2})?")) {
                showAlert(Alert.AlertType.ERROR, "Please input a valid amount!");
                return;
            }
            amount = Double.parseDouble(raw);
            if (amount < totalP) {
                showAlert(Alert.AlertType.ERROR, "Invalid payment amount!");
                return;
            }
        } else {
            amount = totalP;
        }

        double change = amount - totalP;
        menu_change.setText(String.format("£%.2f", change));

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Message");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Charge £%.2f via %s?", totalP, payMethodSel));
        Optional<ButtonType> option = alert.showAndWait();
        if (option.isEmpty() || option.get() != ButtonType.OK) return;

        // snapshot the receipt before the cart is cleared
        lastReceiptContent = buildReceiptString(amount, change, true);

        try {
            lastOrderId = orderRepository.insertReceipt(AppSession.cID, totalP, new java.sql.Date(new Date().getTime()),
                    AppSession.username, orderTypeSel, payMethodSel);

            // preserve the order lines for history / top sellers before clearing the cart
            orderRepository.copyCartToOrderItems(lastOrderId, AppSession.cID);

            orderRepository.clearCart(AppSession.cID);

            if (AppSession.autoPrint) {
                saveReceiptToFile(lastReceiptContent);
            }

            success_msg.setText("Order #" + (lastOrderId > 0 ? lastOrderId : "?") + " has been sent to prep.");
            menu_successPane.setVisible(true);

            menuShowOrderData();
            menuGetTotal();
            recentOrdersRefresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void successPrint() {
        if (!lastReceiptContent.isEmpty() && !AppSession.autoPrint) {
            saveReceiptToFile(lastReceiptContent);
        } else if (!lastReceiptContent.isEmpty()) {
            showReceiptDialog(lastReceiptContent);
        }
    }

    @FXML
    public void successNewOrder() {
        menu_successPane.setVisible(false);
        resetPaymentFields();
        menuShowOrderData();
        menuGetTotal();
    }

    // ---------- Receipt ----------

    private String buildReceiptString(double amountPaid, double change, boolean paid) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===========================================\n");
        receipt.append("             TUSKS MOCHA GARDEN            \n");
        receipt.append("===========================================\n");
        receipt.append("Date: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        receipt.append("Cashier: ").append(AppSession.username).append("\n");
        receipt.append("Customer ID: ").append(AppSession.cID).append("\n");
        receipt.append("Order Type: ").append(orderTypeSel).append("\n");
        receipt.append("Payment: ").append(payMethodSel).append("\n");
        receipt.append("-------------------------------------------\n");
        receipt.append(String.format("%-26s %-4s %-10s\n", "ITEM", "QTY", "PRICE"));
        receipt.append("-------------------------------------------\n");

        for (Product item : menuOrderListData) {
            receipt.append(String.format("%-26s %-4d £%-10.2f\n",
                    truncateString(item.getProductName(), 26),
                    item.getQuantity(),
                    item.getPrice()));
            String opts = item.getType();
            if (opts != null && !opts.isEmpty()) {
                receipt.append("   ").append(truncateString(opts, 40)).append("\n");
            }
        }

        receipt.append("-------------------------------------------\n");
        long totalPence = Math.round(totalP * 100);
        long subtotalPence = Math.round(totalPence / 1.2);
        receipt.append(String.format("SUBTOTAL (ex VAT)%21s\n", String.format("£%.2f", subtotalPence / 100.0)));
        receipt.append(String.format("VAT (20%%)%29s\n", String.format("£%.2f", (totalPence - subtotalPence) / 100.0)));
        receipt.append(String.format("TOTAL%33s\n", String.format("£%.2f", totalP)));

        if (paid) {
            receipt.append(String.format("AMOUNT PAID%27s\n", String.format("£%.2f", amountPaid)));
            receipt.append(String.format("CHANGE%32s\n", String.format("£%.2f", change)));
        }

        receipt.append("===========================================\n");
        receipt.append("          THANK YOU FOR YOUR VISIT!        \n");
        receipt.append("===========================================\n");
        return receipt.toString();
    }

    @FXML
    public void menuReceiptBtn() {
        if (menuOrderListData == null || menuOrderListData.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No order items to print receipt for!");
            return;
        }
        showReceiptDialog(buildReceiptString(0, 0, false));
    }

    private void showReceiptDialog(String content) {
        Alert receiptDialog = new Alert(Alert.AlertType.INFORMATION);
        receiptDialog.setTitle("Receipt");
        receiptDialog.setHeaderText("Tusks Mocha Garden Receipt");
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(500);
        receiptDialog.getDialogPane().setContent(textArea);
        receiptDialog.showAndWait();
    }

    private void saveReceiptToFile(String receiptContent) {
        try {
            File receiptsDir = new File("receipts");
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs();
            }
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("receipt_CID%d_%s.txt", AppSession.cID, timestamp);
            File receiptFile = new File(receiptsDir, filename);
            try (FileWriter writer = new FileWriter(receiptFile)) {
                writer.write(receiptContent);
                writer.flush();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to save receipt to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String truncateString(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }

    // =====================================================================
    // Orders view
    // =====================================================================

    @FXML
    public void ordersTabSwitch(ActionEvent event) {
        ordersTab = event.getSource() == orders_completedBtn ? "Completed" : "Active";
        orders_activeBtn.getStyleClass().remove("seg-active");
        orders_completedBtn.getStyleClass().remove("seg-active");
        ((Button) event.getSource()).getStyleClass().add("seg-active");
        ordersRefresh();
    }

    public void ordersRefresh() {
        orders_listBox.getChildren().clear();
        orders_activeBtn.setText("Active (" + orderRepository.activeOrderCount() + ")");

        List<OrderRepository.OrderSummary> orders = orderRepository.listOrders("Active".equals(ordersTab));
        for (OrderRepository.OrderSummary o : orders) {
            orders_listBox.getChildren().add(buildOrderCard(
                    o.id(), o.status(), o.items(), o.orderType(), o.orderTime(), o.total()));
        }
        if (orders.isEmpty()) {
            Label none = new Label("No orders found.");
            none.getStyleClass().add("subtitle");
            orders_listBox.getChildren().add(none);
        }
    }

    private VBox buildOrderCard(int id, String status, int items, String type, String time, double total) {
        Label idLbl = new Label("#" + id);
        idLbl.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusLbl = new Label(status == null ? "Served" : status.toUpperCase());
        statusLbl.getStyleClass().add(statusChipClass(status));
        HBox top = new HBox(8, idLbl, spacer, statusLbl);
        top.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(items + " items • " + (type == null ? "Takeaway" : type));
        meta.getStyleClass().add("subtitle");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Label timeLbl = new Label(time == null ? "" : time.substring(0, Math.min(5, time.length())));
        timeLbl.getStyleClass().add("subtitle");
        HBox bottom = new HBox(8, meta, spacer2, timeLbl);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, top, bottom);
        card.getStyleClass().add("order-card");
        if (id == selectedOrderId) {
            card.getStyleClass().add("order-card-selected");
        }
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setOnMouseClicked(e -> {
            selectedOrderId = id;
            ordersRefresh();
            ordersShowDetail(id);
        });
        return card;
    }

    private String statusChipClass(String status) {
        if ("Prep".equals(status)) return "chip-prep";
        if ("Ready".equals(status)) return "chip-ready";
        return "chip-served";
    }

    private void ordersShowDetail(int id) {
        Optional<OrderRepository.OrderDetail> detail = orderRepository.orderDetail(id);
        if (detail.isEmpty()) return;
        OrderRepository.OrderDetail d = detail.get();

        String status = d.status();
        String time = d.orderTime();
        od_title.setText("Order #" + id);
        od_meta.setText((d.orderType() == null ? "Takeaway" : d.orderType())
                + (time != null ? " • Placed at " + time.substring(0, Math.min(5, time.length())) : ""));
        od_total.setText(String.format("£%.2f", d.total()));
        od_paidVia.setText("Paid via " + (d.paymentMethod() == null ? "Card" : d.paymentMethod()));
        od_paidVia.setVisible(true);

        od_advanceBtn.setDisable("Served".equals(status) || status == null);
        od_advanceBtn.setText("Prep".equals(status) ? "Mark as Ready"
                : "Ready".equals(status) ? "Mark as Served" : "Order Completed");

        od_linesBox.getChildren().clear();
        List<OrderRepository.OrderLineItem> lines = orderRepository.orderLineItems(id);
        for (OrderRepository.OrderLineItem line : lines) {
            Label name = new Label(line.quantity() + "x " + line.prodName());
            name.getStyleClass().add("card-title");
            name.setStyle("-fx-font-size: 14px;");
            VBox left = new VBox(2, name);
            String opts = line.options();
            if (opts != null && !opts.isEmpty()) {
                Label optLbl = new Label(opts);
                optLbl.getStyleClass().add("cart-opts");
                left.getChildren().add(optLbl);
            }
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label price = new Label(String.format("£%.2f", line.price()));
            price.getStyleClass().add("card-title");
            HBox row = new HBox(10, left, spacer, price);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("order-line");
            row.setPadding(new Insets(12, 16, 12, 16));
            od_linesBox.getChildren().add(row);
        }
        if (lines.isEmpty()) {
            Label none = new Label("No line items recorded for this order.");
            none.getStyleClass().add("subtitle");
            od_linesBox.getChildren().add(none);
        }
    }

    @FXML
    public void ordersAdvance() {
        if (selectedOrderId < 0) return;
        try {
            String status = orderRepository.currentStatus(selectedOrderId);
            if (status == null) return;
            String next = "Prep".equals(status) ? "Ready" : "Ready".equals(status) ? "Served" : null;
            if (next == null) return;
            orderRepository.updateStatus(selectedOrderId, next);
            ordersRefresh();
            ordersShowDetail(selectedOrderId);
            recentOrdersRefresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void recentSeeAll() {
        goToOrdersView();
    }

    private void goToOrdersView() {
        showOnly(orders_form);
        setActiveNav(orders_btn);
        ordersRefresh();
    }

    public void recentOrdersRefresh() {
        if (recent_box == null || Boolean.TRUE.equals(AppSession.isAdmin)) return;
        recent_box.getChildren().clear();
        for (OrderRepository.RecentOrder o : orderRepository.recentOrders()) {
            int id = o.id();
            Label idLbl = new Label("#" + id);
            idLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2A2422;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            String status = o.status();
            Label statusLbl = new Label(status == null ? "SERVED" : status.toUpperCase());
            statusLbl.getStyleClass().add(statusChipClass(status));
            HBox top = new HBox(6, idLbl, spacer, statusLbl);
            top.setAlignment(Pos.CENTER_LEFT);
            Label meta = new Label(o.items() + " items • " + (o.orderType() == null ? "Takeaway" : o.orderType()));
            meta.getStyleClass().add("subtitle");
            meta.setStyle("-fx-font-size: 11px;");
            VBox card = new VBox(4, top, meta);
            card.getStyleClass().add("order-card");
            card.setPadding(new Insets(10, 12, 10, 12));
            card.setOnMouseClicked(e -> {
                selectedOrderId = id;
                goToOrdersView();
                ordersShowDetail(id);
            });
            recent_box.getChildren().add(card);
        }
    }

    // =====================================================================
    // Settings
    // =====================================================================

    public void settingsRefresh() {
        String user = AppSession.username == null ? "User" : AppSession.username;
        set_name.setText(user.substring(0, 1).toUpperCase() + user.substring(1));
        set_avatar.setText(initialsOf(user));
        set_role.setText("ROLE: " + (Boolean.TRUE.equals(AppSession.isAdmin) ? "ADMIN" : "BARISTA"));
        if (AppSession.loginTime != null) {
            set_since.setText("Clocked in since " + AppSession.loginTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }
        syncToggle(tgl_autoPrint, AppSession.autoPrint);
        syncToggle(tgl_showVat, AppSession.showVat);
        syncToggle(tgl_sounds, AppSession.registerSounds);
    }

    @FXML
    public void toggleSetting(ActionEvent event) {
        Button b = (Button) event.getSource();
        if (b == tgl_autoPrint) {
            AppSession.autoPrint = !AppSession.autoPrint;
            syncToggle(b, AppSession.autoPrint);
        } else if (b == tgl_showVat) {
            AppSession.showVat = !AppSession.showVat;
            syncToggle(b, AppSession.showVat);
            applyVatVisibility();
        } else if (b == tgl_sounds) {
            AppSession.registerSounds = !AppSession.registerSounds;
            syncToggle(b, AppSession.registerSounds);
        }
    }

    private void syncToggle(Button b, boolean on) {
        b.getStyleClass().remove("switch-on");
        if (on) {
            b.getStyleClass().add("switch-on");
        }
    }

    private void applyVatVisibility() {
        menu_subtotalRow.setVisible(AppSession.showVat);
        menu_vatRow.setVisible(AppSession.showVat);
    }

    // =====================================================================
    // Navigation / shell
    // =====================================================================

    @FXML
    public void switchForm(ActionEvent event) {
        Object s = event.getSource();
        showOnly(s == dashboard_btn ? dashboard_form
                : s == staff_btn ? staff_form
                : s == inventory_btn ? inventory_form
                : s == menu_btn ? menu_form
                : s == orders_btn ? orders_form
                : settings_form);
        setActiveNav((Button) s);

        if (s == dashboard_btn) {
            loadDashboardData();
        } else if (s == staff_btn) {
            staffShowData();
        } else if (s == inventory_btn) {
            inventoryShowData();
        } else if (s == menu_btn) {
            menuDisplayCard();
            menuShowOrderData();
            menuGetTotal();
        } else if (s == orders_btn) {
            ordersRefresh();
        } else if (s == settings_btn) {
            settingsRefresh();
        }
    }

    private void showOnly(AnchorPane form) {
        dashboard_form.setVisible(form == dashboard_form);
        staff_form.setVisible(form == staff_form);
        inventory_form.setVisible(form == inventory_form);
        menu_form.setVisible(form == menu_form);
        orders_form.setVisible(form == orders_form);
        settings_form.setVisible(form == settings_form);
    }

    private void setActiveNav(Button active) {
        Button[] navs = {dashboard_btn, staff_btn, inventory_btn, menu_btn, orders_btn, settings_btn};
        for (Button b : navs) {
            b.getStyleClass().remove("nav-active");
            if (b == active) {
                b.getStyleClass().add("nav-active");
            }
        }
    }

    private void applyRole() {
        boolean admin = Boolean.TRUE.equals(AppSession.isAdmin);
        Button[] adminNav = {dashboard_btn, staff_btn, inventory_btn};
        Button[] baristaNav = {menu_btn, orders_btn, settings_btn};
        for (Button b : adminNav) {
            b.setVisible(admin);
            b.setManaged(admin);
        }
        for (Button b : baristaNav) {
            b.setVisible(!admin);
            b.setManaged(!admin);
        }
        recent_wrap.setVisible(!admin);
        logout_btn.setText(admin ? "Sign Out" : "Clock Out");
        sidebar_role.setText(admin ? "ADMIN" : "BARISTA");
        sidebar_avatar.setText(initialsOf(AppSession.username));

        if (admin) {
            showOnly(dashboard_form);
            setActiveNav(dashboard_btn);
        } else {
            showOnly(menu_form);
            setActiveNav(menu_btn);
        }
    }

    @FXML
    public void logout() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to " + (Boolean.TRUE.equals(AppSession.isAdmin) ? "sign out" : "clock out") + "?");

            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                AppSession.username = null;
                AppSession.isAdmin = null;
                AppSession.id = null;
                AppSession.cID = null;
                AppSession.path = null;
                AppSession.date = null;
                AppSession.loginTime = null;

                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/tusksmochagarden/login.fxml")));
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setTitle("Tusks Mocha Garden - Login");
                stage.setMinHeight(640);
                stage.setMinWidth(1000);
                stage.setScene(scene);
                stage.show();

                logout_btn.getScene().getWindow().hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayUsername() {
        String user = AppSession.username == null ? "User" : AppSession.username;
        username.setText(user.substring(0, 1).toUpperCase() + user.substring(1));
    }

    // =====================================================================
    // Reports (inventory printing)
    // =====================================================================

    @FXML
    public void inventoryPrintSingleBtn() {
        Product selectedProduct = inventory_tableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.ERROR, "Please select a product to print details for!");
            return;
        }
        printSingleProductToFile(selectedProduct);
    }

    @FXML
    public void inventoryPrintAllBtn() {
        printAllStockToFile();
    }

    private void printSingleProductToFile(Product product) {
        try {
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("product_%s_%s.txt", product.getProductId(), timestamp);
            File reportFile = new File(reportsDir, filename);

            StringBuilder report = new StringBuilder();
            report.append("===========================================\n");
            report.append("         PRODUCT DETAILS REPORT           \n");
            report.append("===========================================\n");
            report.append("Generated: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
            report.append("Generated by: ").append(AppSession.username).append("\n\n");
            report.append("PRODUCT INFORMATION:\n");
            report.append("-------------------------------------------\n");
            report.append("Product ID: ").append(product.getProductId()).append("\n");
            report.append("Product Name: ").append(product.getProductName()).append("\n");
            report.append("Category: ").append(product.getType()).append("\n");
            report.append("Stock Available: ").append(product.getStock()).append(" units\n");
            report.append("Unit Price: £").append(String.format("%.2f", product.getPrice())).append("\n");
            report.append("Total Value: £").append(String.format("%.2f", product.getStock() * product.getPrice())).append("\n");
            report.append("Status: ").append(product.getStatus()).append("\n");
            report.append("Date Added: ").append(product.getDate()).append("\n");
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                report.append("Image Path: ").append(product.getImage()).append("\n");
            }
            report.append("\n===========================================\n");
            report.append("         END OF PRODUCT REPORT            \n");
            report.append("===========================================\n");

            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write(report.toString());
                writer.flush();
            }

            showAlert(Alert.AlertType.INFORMATION,
                    String.format("Product report saved successfully!\n\nFile: %s\nLocation: %s",
                            filename, reportFile.getAbsolutePath()));

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to save product report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printAllStockToFile() {
        try {
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("stock_report_%s.txt", timestamp);
            File reportFile = new File(reportsDir, filename);

            ObservableList<Product> allProducts = inventoryDataList();

            double totalCostValue = 0;
            double totalSellingValue = 0;
            int totalUnits = 0;

            for (Product product : allProducts) {
                totalUnits += product.getStock();
                double productValue = product.getStock() * product.getPrice();
                totalSellingValue += productValue;
                totalCostValue += productValue * 0.7;
            }

            StringBuilder report = new StringBuilder();
            report.append("===========================================\n");
            report.append("           COMPLETE STOCK REPORT          \n");
            report.append("===========================================\n");
            report.append("Generated: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
            report.append("Generated by: ").append(AppSession.username).append("\n\n");
            report.append("STOCK SUMMARY:\n");
            report.append("-------------------------------------------\n");
            report.append("Total Products: ").append(allProducts.size()).append("\n");
            report.append("Total Units in Stock: ").append(totalUnits).append("\n");
            report.append("Total Stock Value (Cost): £").append(String.format("%.2f", totalCostValue)).append("\n");
            report.append("Total Stock Value (Selling): £").append(String.format("%.2f", totalSellingValue)).append("\n");
            report.append("Potential Profit: £").append(String.format("%.2f", totalSellingValue - totalCostValue)).append("\n\n");
            report.append("DETAILED STOCK LISTING:\n");
            report.append("-------------------------------------------\n");
            report.append(String.format("%-15s %-25s %-12s %-8s %-10s %-12s %-15s\n",
                    "Product ID", "Product Name", "Category", "Stock", "Price", "Total Value", "Status"));
            report.append("-------------------------------------------\n");

            for (Product product : allProducts) {
                double totalValue = product.getStock() * product.getPrice();
                report.append(String.format("%-15s %-25s %-12s %-8d £%-9.2f £%-11.2f %-15s\n",
                        truncateString(product.getProductId(), 15),
                        truncateString(product.getProductName(), 25),
                        truncateString(product.getType(), 12),
                        product.getStock(),
                        product.getPrice(),
                        totalValue,
                        product.getStatus()));
            }

            report.append("\n===========================================\n");
            report.append("            END OF STOCK REPORT           \n");
            report.append("===========================================\n");

            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write(report.toString());
                writer.flush();
            }

            showAlert(Alert.AlertType.INFORMATION,
                    String.format("Stock report saved successfully!\n\nFile: %s\nLocation: %s\n\nTotal Products: %d\nTotal Value: £%.2f",
                            filename, reportFile.getAbsolutePath(), allProducts.size(), totalSellingValue));

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to save stock report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // Init
    // =====================================================================

    private void resetPaymentFields() {
        if (menu_amount != null) menu_amount.clear();
        if (menu_change != null) menu_change.setText("£0.00");
        if (menu_payBtn != null) menu_payBtn.setDisable(true);
    }

    private void setupNumericOnlyFields() {
        inventory_stock.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                inventory_stock.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        inventory_price.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                inventory_price.setText(oldValue);
            }
        });

        menu_amount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                menu_amount.setText(oldValue);
            }
        });
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            pos_time.setText(now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            pos_date.setText(now.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM")).toUpperCase());
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void showAlert(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ObservableList<String> typeList = FXCollections.observableArrayList("Beverage", "Meal", "Dessert", "Others");
            inventory_type.setItems(typeList);

            ObservableList<String> statusList = FXCollections.observableArrayList("Available", "Unavailable");
            inventory_status.setItems(statusList);

            menuOrderListData = FXCollections.observableArrayList();

            menu_payBtn.setDisable(true);
            resetPaymentFields();
            setupNumericOnlyFields();

            // Slide-over panel starts offscreen; conditional rows collapse when hidden
            inventory_editPanel.setTranslateX(480);
            for (javafx.scene.Node n : new javafx.scene.Node[]{
                    menu_amountRow, menu_changeRow, menu_walletRow, menu_subtotalRow, menu_vatRow,
                    cust_tempWrap, cust_iceWrap, recent_wrap}) {
                n.managedProperty().bind(n.visibleProperty());
            }
            applyVatVisibility();

            displayUsername();
            applyRole();
            startClock();

            if (Boolean.TRUE.equals(AppSession.isAdmin)) {
                loadDashboardData();
                inventoryShowData();
            } else {
                menuDisplayCard();
                menuShowOrderData();
                menuGetTotal();
                recentOrdersRefresh();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
