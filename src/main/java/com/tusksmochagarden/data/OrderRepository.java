package com.tusksmochagarden.data;

import com.tusksmochagarden.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Data access for the in-progress cart (`customer` table) and placed orders (`receipt`, `order_items`). */
public class OrderRepository {

    public record CartLine(int lineId, double unitPrice) {
    }

    /** Looks up an identical cart line (same product + options) so quantities can be merged. */
    public Optional<CartLine> findExistingCartLine(int customerId, String prodId, String options, double unitPrice) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT id FROM customer WHERE customer_id = ? AND prod_id = ? AND IFNULL(options,'') = ?")) {
            prepare.setInt(1, customerId);
            prepare.setString(2, prodId);
            prepare.setString(3, options == null ? "" : options);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) {
                    return Optional.of(new CartLine(result.getInt("id"), unitPrice));
                }
                return Optional.empty();
            }
        }
    }

    public void mergeCartLine(int lineId, int qtyDelta, double lineTotalDelta) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "UPDATE customer SET quantity = quantity + ?, price = price + ? WHERE id = ?")) {
            prepare.setInt(1, qtyDelta);
            prepare.setDouble(2, lineTotalDelta);
            prepare.setInt(3, lineId);
            prepare.executeUpdate();
        }
    }

    public void insertCartLine(int customerId, String prodId, String prodName, String type, int qty,
                                double lineTotal, java.sql.Date date, String image, String username, String options) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "INSERT INTO customer (customer_id, prod_id, prod_name, type, quantity, price, date, image, em_username, options) "
                             + "VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            prepare.setInt(1, customerId);
            prepare.setString(2, prodId);
            prepare.setString(3, prodName);
            prepare.setString(4, type);
            prepare.setInt(5, qty);
            prepare.setDouble(6, lineTotal);
            prepare.setDate(7, date);
            prepare.setString(8, image);
            prepare.setString(9, username);
            prepare.setString(10, options == null ? "" : options);
            prepare.executeUpdate();
        }
    }

    public void updateCartLine(int lineId, int newQty, double newPrice) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "UPDATE customer SET quantity = ?, price = ? WHERE id = ?")) {
            prepare.setInt(1, newQty);
            prepare.setDouble(2, newPrice);
            prepare.setInt(3, lineId);
            prepare.executeUpdate();
        }
    }

    public void deleteCartLine(int lineId) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("DELETE FROM customer WHERE id = ?")) {
            prepare.setInt(1, lineId);
            prepare.executeUpdate();
        }
    }

    /** Cart lines for the current customer; the customization/options string rides in the "type" slot for display. */
    public ObservableList<Product> cartLines(int customerId) {
        ObservableList<Product> listData = FXCollections.observableArrayList();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT * FROM customer WHERE customer_id = ?")) {
            prepare.setInt(1, customerId);
            try (ResultSet result = prepare.executeQuery()) {
                while (result.next()) {
                    String options = "";
                    try {
                        options = result.getString("options");
                    } catch (SQLException ignored) {
                    }
                    listData.add(new Product(
                            result.getInt("id"),
                            result.getString("prod_id"),
                            result.getString("prod_name"),
                            options == null ? "" : options,
                            result.getInt("quantity"),
                            result.getDouble("price"),
                            result.getString("image"),
                            result.getDate("date")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listData;
    }

    public double cartTotal(int customerId) {
        double total = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT SUM(price) FROM customer WHERE customer_id = ?")) {
            prepare.setInt(1, customerId);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) total = result.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public void clearCart(int customerId) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("DELETE FROM customer WHERE customer_id = ?")) {
            prepare.setInt(1, customerId);
            prepare.executeUpdate();
        }
    }

    public int nextCustomerId() {
        int cID = 1;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT MAX(customer_id) FROM receipt");
             ResultSet result = prepare.executeQuery()) {
            if (result.next()) {
                cID = result.getInt(1) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cID;
    }

    /** Inserts the receipt row and returns its generated id, or -1 if none was generated. */
    public int insertReceipt(int customerId, double total, java.sql.Date date, String username,
                              String orderType, String paymentMethod) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "INSERT INTO receipt (customer_id, total, date, em_username, order_type, payment_method, status, order_time) "
                             + "VALUES (?,?,?,?,?,?, 'Prep', CURTIME())",
                     Statement.RETURN_GENERATED_KEYS)) {
            prepare.setInt(1, customerId);
            prepare.setDouble(2, total);
            prepare.setDate(3, date);
            prepare.setString(4, username);
            prepare.setString(5, orderType);
            prepare.setString(6, paymentMethod);
            prepare.executeUpdate();

            try (ResultSet keys = prepare.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void copyCartToOrderItems(int receiptId, int customerId) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "INSERT INTO order_items (receipt_id, prod_id, prod_name, quantity, price, options) "
                             + "SELECT ?, prod_id, prod_name, quantity, price, IFNULL(options,'') FROM customer WHERE customer_id = ?")) {
            prepare.setInt(1, receiptId);
            prepare.setInt(2, customerId);
            prepare.executeUpdate();
        }
    }

    public record OrderSummary(int id, String status, int items, String orderType, String orderTime, double total) {
    }

    public int activeOrderCount() {
        int count = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT COUNT(id) FROM receipt WHERE IFNULL(status,'Served') <> 'Served'");
             ResultSet result = prepare.executeQuery()) {
            if (result.next()) count = result.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public List<OrderSummary> listOrders(boolean activeOnly) {
        List<OrderSummary> orders = new ArrayList<>();
        String where = activeOnly
                ? "WHERE IFNULL(r.status,'Served') <> 'Served' "
                : "WHERE IFNULL(r.status,'Served') = 'Served' ";
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT r.id, r.total, r.order_time, r.order_type, r.status, "
                             + "(SELECT IFNULL(SUM(oi.quantity),0) FROM order_items oi WHERE oi.receipt_id = r.id) AS items "
                             + "FROM receipt r " + where + "ORDER BY r.id DESC LIMIT 40");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                orders.add(new OrderSummary(
                        result.getInt("id"),
                        result.getString("status"),
                        result.getInt("items"),
                        result.getString("order_type"),
                        result.getString("order_time"),
                        result.getDouble("total")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public record OrderDetail(double total, String orderTime, String orderType, String paymentMethod, String status) {
    }

    public Optional<OrderDetail> orderDetail(int id) {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT total, order_time, order_type, payment_method, status FROM receipt WHERE id = ?")) {
            prepare.setInt(1, id);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) {
                    return Optional.of(new OrderDetail(
                            result.getDouble("total"),
                            result.getString("order_time"),
                            result.getString("order_type"),
                            result.getString("payment_method"),
                            result.getString("status")));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public record OrderLineItem(String prodName, int quantity, double price, String options) {
    }

    public List<OrderLineItem> orderLineItems(int receiptId) {
        List<OrderLineItem> lines = new ArrayList<>();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT prod_name, quantity, price, options FROM order_items WHERE receipt_id = ?")) {
            prepare.setInt(1, receiptId);
            try (ResultSet result = prepare.executeQuery()) {
                while (result.next()) {
                    lines.add(new OrderLineItem(
                            result.getString("prod_name"),
                            result.getInt("quantity"),
                            result.getDouble("price"),
                            result.getString("options")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public String currentStatus(int id) {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT status FROM receipt WHERE id = ?")) {
            prepare.setInt(1, id);
            try (ResultSet result = prepare.executeQuery()) {
                return result.next() ? result.getString("status") : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("UPDATE receipt SET status = ? WHERE id = ?")) {
            prepare.setString(1, status);
            prepare.setInt(2, id);
            prepare.executeUpdate();
        }
    }

    public record RecentOrder(int id, String status, String orderType, int items) {
    }

    public List<RecentOrder> recentOrders() {
        List<RecentOrder> orders = new ArrayList<>();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT r.id, r.status, r.order_type, "
                             + "(SELECT IFNULL(SUM(oi.quantity),0) FROM order_items oi WHERE oi.receipt_id = r.id) AS items "
                             + "FROM receipt r ORDER BY r.id DESC LIMIT 3");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                orders.add(new RecentOrder(
                        result.getInt("id"),
                        result.getString("status"),
                        result.getString("order_type"),
                        result.getInt("items")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }
}
