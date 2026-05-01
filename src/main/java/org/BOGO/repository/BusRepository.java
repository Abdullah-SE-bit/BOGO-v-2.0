package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.BusStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusRepository {
    public Bus findById(int busID) {
        String sql = "SELECT BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity FROM BUS WHERE BusId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBus(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    public Bus findFirstByStatus(BusStatus status) {
        String sql = "SELECT TOP 1 BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity FROM BUS WHERE BusStatus = ? ORDER BY BusId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBus(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] findFirstByStatus failed: " + e.getMessage());
        }
        return null;
    }

    public List<Bus> findAll() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity FROM BUS ORDER BY BusId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                buses.add(mapBus(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] findAll failed: " + e.getMessage());
        }
        return buses;
    }

    public int save(Bus bus) {
        String sql = "INSERT INTO BUS (BusCompany, Registration, RegistrationYear, BusStatus, Capacity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bus.getBusCompany());
            ps.setString(2, bus.getRegistration());
            ps.setInt(3, bus.getRegistrationYear());
            ps.setString(4, bus.getBusStatus().name());
            ps.setInt(5, bus.getCapacity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateStatus(int busID, BusStatus status) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return updateStatus(conn, busID, status);
        } catch (SQLException e) {
            System.err.println("[BusRepository] updateStatus failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(Connection conn, int busID, BusStatus status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE BUS SET BusStatus = ? WHERE BusId = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, busID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignDriver(int busID, int driverID) {
        return true;
    }

    public boolean assignRoute(int busID, int routeID) {
        return true;
    }

    private Bus mapBus(ResultSet rs) throws SQLException {
        String rawStatus = rs.getString("BusStatus");
        BusStatus status;
        try {
            status = rawStatus == null ? BusStatus.AVAILABLE : BusStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            status = BusStatus.AVAILABLE;
        }
        return new Bus(
                rs.getInt("BusId"),
                rs.getString("BusCompany"),
                rs.getString("Registration"),
                rs.getInt("RegistrationYear"),
                status,
                rs.getInt("Capacity")
        );
    }
}
