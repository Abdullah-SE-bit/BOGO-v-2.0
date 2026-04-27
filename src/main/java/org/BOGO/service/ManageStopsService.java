package org.BOGO.service;

import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Admin;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;
import java.util.List;

public class ManageStopsService {

    private final StopRepository  stopRepository;
    private final RouteRepository routeRepository;

    public ManageStopsService(StopRepository stopRepository, RouteRepository routeRepository) {
        this.stopRepository  = stopRepository;
        this.routeRepository = routeRepository;
    }

    /**
     * Validates and persists a new stop, then propagates it to the routing engine.
     */
    public void addStop(Stop stop, Admin admin) {}

    /**
     * Updates stop details (name, location, active flag) and propagates changes.
     */
    public void editStop(int stopID, Stop updatedData, Admin admin) {}

    /**
     * Removes a stop after verifying no active bookings depend on it.
     */
    public void removeStop(int stopID, Admin admin) {}

    /**
     * Checks for duplicate name/coordinates and valid coordinate range.
     */
    public boolean validateStop(Stop stop) {
        return false;
    }

    /**
     * Pushes stop changes to the routing engine, driver portals, and passenger app.
     */
    public void propagateChanges(Stop stop, Route route) {}

    /**
     * Returns all stops belonging to the given route.
     */
    public List<Stop> getStopsByRoute(int routeID) {
        return null;
    }
}
