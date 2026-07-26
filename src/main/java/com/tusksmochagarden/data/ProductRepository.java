package com.tusksmochagarden.data;

import com.tusksmochagarden.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Data access for the `product` table — inventory CRUD and the POS/menu product listing. */
public class ProductRepository {

    public ObservableList<Product> findAllForInventory() {
        ObservableList<Product> listData = FXCollections.observableArrayList();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT * FROM product");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                listData.add(new Product(
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

    public ObservableList<Product> findAllForMenu() {
        ObservableList<Product> listData = FXCollections.observableArrayList();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT * FROM product");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                listData.add(new Product(
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

    public boolean existsById(String productId) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT prod_id FROM product WHERE prod_id = ?")) {
            prepare.setString(1, productId);
            try (ResultSet result = prepare.executeQuery()) {
                return result.next();
            }
        }
    }

    public void insert(String productId, String name, String type, int stock, double price,
                        String status, String imagePath, java.sql.Date date) throws SQLException {
        String insertProduct = "INSERT INTO product "
                + "(prod_id, prod_name, type, stock, price, status, image, date) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(insertProduct)) {
            prepare.setString(1, productId);
            prepare.setString(2, name);
            prepare.setString(3, type);
            prepare.setInt(4, stock);
            prepare.setDouble(5, price);
            prepare.setString(6, status);
            prepare.setString(7, imagePath);
            prepare.setDate(8, date);
            prepare.executeUpdate();
        }
    }

    public void update(String productId, String name, String type, int stock, double price,
                        String status, String imagePath, java.sql.Date date) throws SQLException {
        String updateProduct = "UPDATE product SET "
                + "prod_name = ?, type = ?, stock = ?, price = ?, "
                + "status = ?, image = ?, date = ? WHERE prod_id = ?";
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(updateProduct)) {
            prepare.setString(1, name);
            prepare.setString(2, type);
            prepare.setInt(3, stock);
            prepare.setDouble(4, price);
            prepare.setString(5, status);
            prepare.setString(6, imagePath);
            prepare.setDate(7, date);
            prepare.setString(8, productId);
            prepare.executeUpdate();
        }
    }

    public void delete(String productId) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("DELETE FROM product WHERE prod_id = ?")) {
            prepare.setString(1, productId);
            prepare.executeUpdate();
        }
    }

    public record StockAndStatus(int stock, String status) {
    }

    public StockAndStatus stockAndStatus(String productId) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT stock, status FROM product WHERE prod_id = ?")) {
            prepare.setString(1, productId);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) {
                    return new StockAndStatus(result.getInt("stock"), result.getString("status"));
                }
                return new StockAndStatus(0, "");
            }
        }
    }

    public int currentStock(String productId) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT stock FROM product WHERE prod_id = ?")) {
            prepare.setString(1, productId);
            try (ResultSet result = prepare.executeQuery()) {
                return result.next() ? result.getInt("stock") : 0;
            }
        }
    }

    /** Applies a stock delta and keeps the Available/Unavailable status in sync. */
    public void adjustStock(String prodId, int delta) throws SQLException {
        try (Connection connect = Database.connectDB()) {
            try (PreparedStatement prepare = connect.prepareStatement(
                    "UPDATE product SET stock = stock + ? WHERE prod_id = ?")) {
                prepare.setInt(1, delta);
                prepare.setString(2, prodId);
                prepare.executeUpdate();
            }
            try (PreparedStatement prepare = connect.prepareStatement(
                    "UPDATE product SET status = 'Unavailable' WHERE prod_id = ? AND stock <= 0")) {
                prepare.setString(1, prodId);
                prepare.executeUpdate();
            }
            try (PreparedStatement prepare = connect.prepareStatement(
                    "UPDATE product SET status = 'Available' WHERE prod_id = ? AND stock > 0 AND status = 'Unavailable'")) {
                prepare.setString(1, prodId);
                prepare.executeUpdate();
            }
        }
    }
}
