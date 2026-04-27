package org.BOGO.service;

import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.UserRepository;
import java.util.List;

public class HandleDriverService {

    private final UserRepository  userRepository;
    private final BusRepository   busRepository;
    private final RouteRepository routeRepository;

    public HandleDriverService(UserRepository userRepository,
                               BusRepository busRepository,
                               RouteRepository routeRepository) {
        this.userRepository  = userRepository;
        this.busRepository   = busRepository;
        this.routeRepository = routeRepository;
    }

    /**
     * Persists a new driver record created by the admin.
     */
    public void addDriver(Driver driver, Admin admin) {}

    /**
     * Updates the profile or availability status of an existing driver.
     */
    public void updateDriver(int driverID, Driver updatedData, Admin admin) {}

    /**
     * Deactivates the specified driver, preventing new route assignments.
     * Warns if the driver is currently on an active route.
     */
    public void deactivateDriver(int driverID, Admin admin) {}

    /**
     * Links a driver to a specific bus for the shift.
     */
    public void assignDriverToBus(Driver driver, Bus bus) {}

    /**
     * Links a driver to a specific route for the shift.
     */
    public void assignDriverToRoute(Driver driver, Route route) {}

    /**
     * Returns all drivers whose status is ACTIVE and not currently on a route.
     */
    public List<Driver> getAvailableDrivers() {
        return null;
    }
}
