package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.user.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for Driver-related DB operations.
 * Drivers table stores licenseNumber and status.
 * The Users table holds all common user fields.
 */
public class DriverRepository {

    /**
     * Inserts a Drivers row after a Users row has already been inserted.
     * Expects that userRepository.save() was called first.
     */
    public boolean save(int userID, String licenseNumber) {
        String sql = "INSERT INTO Drivers (userID, licenseNumber, status) VALUES (?, ?, 'INACTIVE')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ps.setString(2, licenseNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DriverRepository] save failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates the driver status (ACTIVE / INACTIVE / ON_DUTY).
     */
    public boolean updateStatus(int driverID, String status) {
        String sql = "UPDATE Drivers SET status = ? WHERE userID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, driverID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DriverRepository] updateStatus failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates the profile of a driver in the Users table (name, phone).
     */
    public boolean updateProfile(int driverID, String name, String phoneNumber) {
        String sql = "UPDATE Users SET name = ?, phoneNumber = ? WHERE userID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phoneNumber);
            ps.setInt(3, driverID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DriverRepository] updateProfile failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns all drivers whose status is INACTIVE (available for assignment).
     */
    public List<Integer> findAvailableDriverIds() {
        String sql = "SELECT userID FROM Drivers WHERE status = 'INACTIVE'";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt("userID"));
        } catch (SQLException e) {
            System.err.println("[DriverRepository] findAvailableDriverIds failed: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Returns the license number + status for a given driverID.
     * Returns null if not found.
     */
    public ResultSet findRawById(int driverID, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT d.licenseNumber, d.status, u.name, u.email, u.phoneNumber " +
            "FROM Drivers d JOIN Users u ON u.userID = d.userID WHERE d.userID = ?");
        ps.setInt(1, driverID);
        return ps.executeQuery();
    }
}
