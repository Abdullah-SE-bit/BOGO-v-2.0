package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.transport.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for Stop-related DB operations.
 */
public class StopRepository {

    /**
     * Returns all stops that belong to the given routeID via the RouteStops join table.
     */
    public List<Stop> findByRouteId(int routeID) {
        String sql = "SELECT s.stopID, s.stopName, s.active, s.locationX, s.locationY " +
                     "FROM Stops s " +
                     "JOIN RouteStops rs ON rs.stopID = s.stopID " +
                     "WHERE rs.routeID = ? ORDER BY rs.sequenceNumber";
        List<Stop> stops = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stop stop = new Stop();
                    stop.initializeStop(
                        rs.getInt("stopID"),
                        rs.getString("stopName"),
                        new Location(rs.getInt("locationX"), rs.getInt("locationY"))
                    );
                    stops.add(stop);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] findByRouteId failed: " + e.getMessage());
        }
        return stops;
    }

    /**
     * Finds a Stop by its ID.
     */
    public Stop findById(int stopID) {
        String sql = "SELECT stopID, stopName, active, locationX, locationY FROM Stops WHERE stopID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stopID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Stop stop = new Stop();
                    stop.initializeStop(
                        rs.getInt("stopID"),
                        rs.getString("stopName"),
                        new Location(rs.getInt("locationX"), rs.getInt("locationY"))
                    );
                    return stop;
                }
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns true if a stop with the same name or same coordinates already exists.
     * Used for duplicate checking in validateStop().
     */
    public boolean existsByNameOrLocation(String stopName, int locationX, int locationY) {
        String sql = "SELECT COUNT(*) FROM Stops WHERE stopName = ? OR (locationX = ? AND locationY = ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stopName);
            ps.setInt(2, locationX);
            ps.setInt(3, locationY);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] existsByNameOrLocation failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks whether any active booking path includes the given stopID.
     * Used before deletion.
     */
    public boolean hasActiveBookings(int stopID) {
        // Bookings reference buses, buses are on routes, routes have stops.
        // We check RouteStops membership and any PENDING/active Booking on that bus.
        String sql = "SELECT COUNT(*) FROM Bookings b " +
                     "JOIN Buses bu ON bu.busID = b.busID " +
                     "JOIN RouteStops rs ON rs.routeID = bu.routeID " +
                     "WHERE rs.stopID = ? AND b.active = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stopID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] hasActiveBookings failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Persists a new Stop and returns the generated stopID, or -1 on failure.
     */
    public int save(Stop stop, int locationX, int locationY) {
        String sql = "INSERT INTO Stops (stopName, active, locationX, locationY) VALUES (?, 1, ?, ?); " +
                     "SELECT SCOPE_IDENTITY();";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, stop.getStopName());
            ps.setInt(2, locationX);
            ps.setInt(3, locationY);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates name, locationX, locationY, and active flag for the given stopID.
     */
    public boolean update(int stopID, String newName, int locationX, int locationY, boolean active) {
        String sql = "UPDATE Stops SET stopName = ?, locationX = ?, locationY = ?, active = ? WHERE stopID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, locationX);
            ps.setInt(3, locationY);
            ps.setBoolean(4, active);
            ps.setInt(5, stopID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StopRepository] update failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a Stop record and its RouteStops entries (cascade-style).
     */
    public boolean delete(int stopID) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM RouteStops WHERE stopID = ?")) {
                    ps1.setInt(1, stopID);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM Stops WHERE stopID = ?")) {
                    ps2.setInt(1, stopID);
                    ps2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[StopRepository] delete transaction rolled back: " + ex.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] delete failed: " + e.getMessage());
        }
        return false;
    }
}
