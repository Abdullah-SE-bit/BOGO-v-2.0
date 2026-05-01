package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;

import java.sql.*;

/**
 * JDBC repository for Route-related DB operations.
 */
public class RouteRepository {

    /**
     * Returns true if the given routeID exists in the Routes table.
     */
    public boolean routeExists(int routeID) {
        String sql = "SELECT COUNT(*) FROM Routes WHERE routeID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] routeExists failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Inserts a stop into a route in the RouteStops join table.
     * sequenceNumber determines the stop's order on the route.
     */
    public boolean addStopToRoute(int routeID, int stopID, double price, int sequenceNumber) {
        String sql = "INSERT INTO RouteStops (routeID, stopID, price, sequenceNumber) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            ps.setInt(2, stopID);
            ps.setDouble(3, price);
            ps.setInt(4, sequenceNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteRepository] addStopToRoute failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Removes a stop from a route in the RouteStops join table.
     */
    public boolean removeStopFromRoute(int routeID, int stopID) {
        String sql = "DELETE FROM RouteStops WHERE routeID = ? AND stopID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            ps.setInt(2, stopID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteRepository] removeStopFromRoute failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Assigns a bus to a route (UPDATE Buses SET routeID = ?).
     */
    public boolean assignBusToRoute(int busID, int routeID) {
        String sql = "UPDATE Buses SET routeID = ? WHERE busID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            ps.setInt(2, busID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteRepository] assignBusToRoute failed: " + e.getMessage());
        }
        return false;
    }
}
