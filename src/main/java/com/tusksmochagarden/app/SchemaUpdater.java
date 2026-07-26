package com.tusksmochagarden.app;

import com.tusksmochagarden.data.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Idempotent, in-app schema migration for the tusks-modern redesign.
 * Runs at startup; every step is safe to re-run and failures are non-fatal
 * so the app still opens when the database is unreachable.
 */
public final class SchemaUpdater {

    private SchemaUpdater() {
    }

    public static void ensureSchema() {
        Connection conn = Database.connectDB();
        if (conn == null) {
            System.err.println("SchemaUpdater: no database connection, skipping migration.");
            return;
        }
        try {
            createOrderItemsTable(conn);
            addColumnIfMissing(conn, "receipt", "order_type", "VARCHAR(20) DEFAULT 'Takeaway'");
            addColumnIfMissing(conn, "receipt", "payment_method", "VARCHAR(20) DEFAULT 'Card'");
            addColumnIfMissing(conn, "receipt", "status", "VARCHAR(20) DEFAULT 'Prep'");
            addColumnIfMissing(conn, "receipt", "order_time", "TIME NULL");
            addColumnIfMissing(conn, "customer", "options", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, "employee", "last_active", "DATETIME NULL");
        } catch (Exception e) {
            System.err.println("SchemaUpdater: migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createOrderItemsTable(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS order_items ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "receipt_id INT NOT NULL, "
                    + "prod_id VARCHAR(50), "
                    + "prod_name VARCHAR(100), "
                    + "quantity INT, "
                    + "price DOUBLE, "
                    + "options VARCHAR(255), "
                    + "INDEX idx_receipt (receipt_id))");
        }
    }

    private static void addColumnIfMissing(Connection conn, String table, String column, String definition) throws SQLException {
        String check = "SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            System.out.println("SchemaUpdater: added " + table + "." + column);
        }
    }
}
