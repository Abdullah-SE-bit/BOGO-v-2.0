package org.BOGO.service;

import org.BOGO.domain.communication.Message;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.repository.RouteRepository;

public class RouteReviseService {

    private final RouteRepository routeRepository;

    public RouteReviseService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Applies admin-approved changes to the route and persists the result.
     * Returns the updated Route.
     */
    public Route reviseRoute(Route route, Admin admin) {
        return null;
    }

    /**
     * Inserts a new stop at the correct sequence position in the route.
     */
    public void addStopToRoute(Route route, Stop stop) {}

    /**
     * Removes a stop from the route after verifying no active bookings depend on it.
     */
    public void removeStopFromRoute(Route route, Stop stop) {}

    /**
     * Validates that the route forms a continuous, non-duplicate sequence of stops.
     */
    public boolean validateRoute(Route route) {
        return false;
    }

    /**
     * Pushes a real-time route-change notification to all drivers on this route.
     */
    public void notifyDrivers(Route route, Message message) {}
}
