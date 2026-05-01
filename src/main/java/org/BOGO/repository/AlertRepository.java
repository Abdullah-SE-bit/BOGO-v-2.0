package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.communication.Alert;

import java.sql.*;

public class AlertRepository {
    public int save(Alert alert) {
        String sql = "INSERT INTO ALERTS (SenderDriverId, AlertType, Priority, Message, Status, SentTime) VALUES (?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, alert.getSenderDriverId());
            ps.setString(2, alert.getAlertType());
            ps.setString(3, alert.getPriority());
            ps.setString(4, alert.getMessage());
            ps.setString(5, alert.getStatus() == null ? "OPEN" : alert.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AlertRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    public Alert findById(int alertId) {
        String sql = "SELECT AlertId, SenderDriverId, AlertType, Priority, Message, Status, SentTime FROM ALERTS WHERE AlertId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp sentTime = rs.getTimestamp("SentTime");
                    return new Alert(
                            rs.getInt("AlertId"),
                            rs.getInt("SenderDriverId"),
                            rs.getString("AlertType"),
                            rs.getString("Priority"),
                            rs.getString("Message"),
                            rs.getString("Status"),
                            sentTime == null ? null : sentTime.toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[AlertRepository] findById failed: " + e.getMessage());
        }
        return null;
    }
}
