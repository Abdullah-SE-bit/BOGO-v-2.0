package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteRepository {
    public boolean routeExists(int routeID) {
        String sql = "SELECT COUNT(*) FROM ROUTEE WHERE RouteId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] routeExists failed: " + e.getMessage());
            return false;
        }
    }

    public List<Route> findAll() {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT RouteId, Active, Stop_IDs FROM ROUTEE ORDER BY RouteId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Route route = new Route(rs.getInt("RouteId"));
                route.setStopIDs(parseIds(rs.getString("Stop_IDs")));
                routes.add(route);
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] findAll failed: " + e.getMessage());
        }
        return routes;
    }

    public boolean addStopToRoute(int routeID, int stopID, double price, int sequenceNumber) {
        Route route = findById(routeID);
        if (route == null) {
            return false;
        }
        ArrayList<Integer> ids = route.getStopIDs();
        if (!ids.contains(stopID)) {
            ids.add(stopID);
        }
        return updateStopIds(routeID, ids);
    }

    public boolean removeStopFromRoute(int routeID, int stopID) {
        Route route = findById(routeID);
        if (route == null) {
            return false;
        }
        ArrayList<Integer> ids = route.getStopIDs();
        ids.remove(Integer.valueOf(stopID));
        return updateStopIds(routeID, ids);
    }

    public boolean assignBusToRoute(int busID, int routeID) {
        return routeExists(routeID);
    }

    public Route findById(int routeID) {
        String sql = "SELECT RouteId, Stop_IDs FROM ROUTEE WHERE RouteId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Route route = new Route(rs.getInt("RouteId"));
                    route.setStopIDs(parseIds(rs.getString("Stop_IDs")));
                    return route;
                }
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    private boolean updateStopIds(int routeID, List<Integer> stopIds) {
        String sql = "UPDATE ROUTEE SET Stop_IDs = ? WHERE RouteId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, joinIds(stopIds));
            ps.setInt(2, routeID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteRepository] updateStopIds failed: " + e.getMessage());
            return false;
        }
    }

    static ArrayList<Integer> parseIds(String raw) {
        ArrayList<Integer> ids = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        for (String token : raw.split(",")) {
            try {
                ids.add(Integer.parseInt(token.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    static String joinIds(List<Integer> ids) {
        StringBuilder builder = new StringBuilder();
        for (Integer id : ids) {
            if (id == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }
}
