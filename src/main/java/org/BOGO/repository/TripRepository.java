package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.BusStatus;
import org.BOGO.domain.transport.Trip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {
    public Trip findById(int tripId) {
        String sql = "SELECT TripId, RouteId, BusId, DriverId, DepartureTime, ArrivalTime FROM TRIP WHERE TripId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTrip(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TripRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    public List<Trip> findLiveTripsWithBuses() {
        List<Trip> trips = new ArrayList<>();
        String sql = """
                SELECT t.TripId, t.RouteId, t.BusId, t.DriverId, t.DepartureTime, t.ArrivalTime,
                       b.BusCompany, b.Registration, b.RegistrationYear, b.BusStatus, b.Capacity
                FROM TRIP t
                JOIN BUS b ON b.BusId = t.BusId
                WHERE t.ArrivalTime IS NULL OR t.ArrivalTime >= GETDATE()
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Trip trip = mapTrip(rs);
                trip.setBus(new Bus(
                        rs.getInt("BusId"),
                        rs.getString("BusCompany"),
                        rs.getString("Registration"),
                        rs.getInt("RegistrationYear"),
                        parseStatus(rs.getString("BusStatus")),
                        rs.getInt("Capacity")
                ));
                trips.add(trip);
            }
        } catch (SQLException e) {
            System.err.println("[TripRepository] findLiveTripsWithBuses failed: " + e.getMessage());
        }
        return trips;
    }

    public boolean swapBus(Connection conn, int oldBusId, int newBusId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE TRIP SET BusId = ? WHERE BusId = ?")) {
            ps.setInt(1, newBusId);
            ps.setInt(2, oldBusId);
            return ps.executeUpdate() > 0;
        }
    }

    private Trip mapTrip(ResultSet rs) throws SQLException {
        Timestamp departure = rs.getTimestamp("DepartureTime");
        Timestamp arrival = rs.getTimestamp("ArrivalTime");
        return new Trip(
                rs.getInt("TripId"),
                rs.getInt("RouteId"),
                rs.getInt("BusId"),
                rs.getInt("DriverId"),
                departure == null ? null : departure.toLocalDateTime(),
                arrival == null ? null : arrival.toLocalDateTime()
        );
    }

    private BusStatus parseStatus(String value) {
        try {
            return value == null ? BusStatus.AVAILABLE : BusStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return BusStatus.AVAILABLE;
        }
    }
}
