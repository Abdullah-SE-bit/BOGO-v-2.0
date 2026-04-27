package org.BOGO.service;

import org.BOGO.domain.communication.Message;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Location;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.UserRepository;

public class IssueReportingService {

    private final BusRepository  busRepository;
    private final UserRepository userRepository;

    public IssueReportingService(BusRepository busRepository, UserRepository userRepository) {
        this.busRepository  = busRepository;
        this.userRepository = userRepository;
    }

    /**
     * Records an incident report for the driver at the given location.
     * Returns the generated unique incident ID string.
     */
    public String reportIssue(Driver driver, String type, String details, Location location) {
        return null;
    }

    /**
     * Updates the bus status to BUS_DOWN / DRIVER_DOWN and suspends
     * further stop assignments.
     */
    public void flagBus(Bus bus) {}

    /**
     * Sends an immediate alert to the admin portal with incident details.
     */
    public void notifyAdmin(Admin admin, String incidentID) {}

    /**
     * Sends a disruption notification to all passengers with active bookings
     * on the affected bus.
     */
    public void notifyPassengers(Bus bus, Message message) {}
}
