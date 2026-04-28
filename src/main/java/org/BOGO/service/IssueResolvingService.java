package org.BOGO.service;

import org.BOGO.domain.communication.Message;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.UserRepository;

public class IssueResolvingService {

    private final BusRepository  busRepository = new BusRepository();
    private final UserRepository userRepository = new UserRepository();


    /**
     * Retrieves the full incident message/log for the given incident ID.
     */
    public Message getIncident(String incidentID) {
        return null;
    }

    /**
     * Records the dispatch of a backup driver against the incident and
     * notifies all relevant parties.
     */
    public void dispatchBackup(Admin admin, Driver backupDriver, String incidentID) {}

    /**
     * Sends a confirmation message to the affected driver that help is on the way.
     */
    public void notifyAffectedDriver(Driver driver, Message message) {}

    /**
     * Broadcasts a rerouting notification to all affected passengers.
     */
    public void notifyPassengers(Bus bus, Message message) {}

    /**
     * Closes the incident, logs resolution details, and restores normal
     * service status for the route.
     */
    public void resolveIncident(String incidentID) {}

    /**
     * Transfers the active route and pending stop list from one driver to another.
     */
    public void transferRoute(Driver fromDriver, Driver toDriver) {}
}
