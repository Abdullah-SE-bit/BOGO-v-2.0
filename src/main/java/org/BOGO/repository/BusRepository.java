package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Route;

import java.sql.*;

/**
 * JDBC repository for Bus-related DB operations.
 */
public class BusRepository {

    /**
     * Finds a Bus record by its busID.
     * Returns a Bus shell with the ID and capacity set, or null if not found.
     */
    public Bus findById(int busID) {
        String sql = "SELECT busID, capacity, currentCapacity, routeID, driverID FROM Buses WHERE busID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bus bus = new Bus(rs.getInt("busID"), rs.getInt("capacity"));
                    bus.setCurrentCapacity(rs.getInt("currentCapacity"));
                    return bus;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Links a driver to a bus (UPDATE Buses SET driverID = ? WHERE busID = ?).
     * Returns true if the update succeeded.
     */
    public boolean assignDriver(int busID, int driverID) {
        String sql = "UPDATE Buses SET driverID = ? WHERE busID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverID);
            ps.setInt(2, busID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BusRepository] assignDriver failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Links a route to a bus (UPDATE Buses SET routeID = ? WHERE busID = ?).
     * Returns true if the update succeeded.
     */
    public boolean assignRoute(int busID, int routeID) {
        String sql = "UPDATE Buses SET routeID = ? WHERE busID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            ps.setInt(2, busID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BusRepository] assignRoute failed: " + e.getMessage());
        }
        return false;
    }
}
