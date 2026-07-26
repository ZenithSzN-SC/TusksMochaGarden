package com.tusksmochagarden.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Data access for the dashboard's aggregate stats, alerts, and chart series (reads `receipt`, `order_items`, `product`). */
public class DashboardRepository {

    public int newCustomersToday(java.sql.Date date) {
        int nc = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT COUNT(id) FROM receipt WHERE date = ?")) {
            prepare.setDate(1, date);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) nc = result.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nc;
    }

    public double totalIncomeForDate(java.sql.Date date) {
        double ti = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT SUM(total) FROM receipt WHERE date = ?")) {
            prepare.setDate(1, date);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) ti = result.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ti;
    }

    public double totalIncomeAllTime() {
        double ti = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT SUM(total) FROM receipt");
             ResultSet result = prepare.executeQuery()) {
            if (result.next()) ti = result.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ti;
    }

    public double averageOrderValueForDate(java.sql.Date date) {
        double aov = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT AVG(total) FROM receipt WHERE date = ?")) {
            prepare.setDate(1, date);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) aov = result.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aov;
    }

    public double receiptSumForDate(java.sql.Date date) {
        double sum = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT SUM(total) FROM receipt WHERE date = ?")) {
            prepare.setDate(1, date);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) sum = result.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }

    public int receiptCountForDate(java.sql.Date date) {
        int count = 0;
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT COUNT(id) FROM receipt WHERE date = ?")) {
            prepare.setDate(1, date);
            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) count = result.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public record LowStockAlert(String productName, int stock) {
    }

    public List<LowStockAlert> lowStockAlerts(int threshold) {
        List<LowStockAlert> alerts = new ArrayList<>();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT prod_name, stock FROM product WHERE stock <= ? OR status = 'Unavailable' ORDER BY stock ASC")) {
            prepare.setInt(1, threshold);
            try (ResultSet result = prepare.executeQuery()) {
                while (result.next()) {
                    alerts.add(new LowStockAlert(result.getString("prod_name"), result.getInt("stock")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alerts;
    }

    public record TopSeller(String productName, int quantitySold, double revenue) {
    }

    public List<TopSeller> topSellersToday() {
        List<TopSeller> sellers = new ArrayList<>();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT oi.prod_name, SUM(oi.quantity) AS qty, SUM(oi.price) AS revenue "
                             + "FROM order_items oi JOIN receipt r ON r.id = oi.receipt_id "
                             + "WHERE r.date = CURDATE() GROUP BY oi.prod_name ORDER BY qty DESC LIMIT 3");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                sellers.add(new TopSeller(result.getString("prod_name"), result.getInt("qty"), result.getDouble("revenue")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sellers;
    }

    public record ChartPoint(String label, double value) {
    }

    public List<ChartPoint> incomeLast7Days() {
        List<ChartPoint> points = new ArrayList<>();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT date, SUM(total) FROM receipt WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY date ORDER BY TIMESTAMP(date)");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                points.add(new ChartPoint(result.getString(1), result.getFloat(2)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return points;
    }

    public List<ChartPoint> ordersLast7Days() {
        List<ChartPoint> points = new ArrayList<>();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT date, COUNT(id) FROM receipt WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY date ORDER BY TIMESTAMP(date)");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                points.add(new ChartPoint(result.getString(1), result.getInt(2)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return points;
    }
}
