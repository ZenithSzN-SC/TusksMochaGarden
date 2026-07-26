package com.tusksmochagarden.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Data access for the `employee` table — staff listing and account management. */
public class StaffRepository {

    public record StaffRow(String username, boolean isAdmin, Timestamp lastActive) {
    }

    public List<StaffRow> listStaff() throws SQLException {
        List<StaffRow> rows = new ArrayList<>();
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "SELECT username, is_admin, last_active FROM employee ORDER BY is_admin DESC, username");
             ResultSet result = prepare.executeQuery()) {
            while (result.next()) {
                rows.add(new StaffRow(
                        result.getString("username"),
                        result.getBoolean("is_admin"),
                        result.getTimestamp("last_active")));
            }
        }
        return rows;
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("SELECT username FROM employee WHERE username = ?")) {
            prepare.setString(1, username);
            try (ResultSet result = prepare.executeQuery()) {
                return result.next();
            }
        }
    }

    public void addStaff(String username, String hashedPassword, String question, String answer, boolean isAdmin) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "INSERT INTO employee (username, password, question, answer, is_admin, hire_date) VALUES (?,?,?,?,?,?)")) {
            prepare.setString(1, username);
            prepare.setString(2, hashedPassword);
            prepare.setString(3, question);
            prepare.setString(4, answer);
            prepare.setBoolean(5, isAdmin);
            prepare.setDate(6, new java.sql.Date(new Date().getTime()));
            prepare.executeUpdate();
        }
    }

    public void removeStaff(String username) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("DELETE FROM employee WHERE username = ?")) {
            prepare.setString(1, username);
            prepare.executeUpdate();
        }
    }

    public void updateRole(String username, boolean isAdmin) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement("UPDATE employee SET is_admin = ? WHERE username = ?")) {
            prepare.setBoolean(1, isAdmin);
            prepare.setString(2, username);
            prepare.executeUpdate();
        }
    }

    public void updateRoleAndPassword(String username, boolean isAdmin, String hashedPassword) throws SQLException {
        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(
                     "UPDATE employee SET is_admin = ?, password = ? WHERE username = ?")) {
            prepare.setBoolean(1, isAdmin);
            prepare.setString(2, hashedPassword);
            prepare.setString(3, username);
            prepare.executeUpdate();
        }
    }
}
