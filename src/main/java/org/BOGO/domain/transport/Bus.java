package org.BOGO.domain.transport;
import org.BOGO.domain.user.*;


public class Bus {

    private int busID;

    private Route route;

    private BusStatus busStatus;

    private int capacity;

    private int currentCapacity;

    private Location location;

    // ---------- Constructors ----------
    public Bus(int ID, int cap) {
        busID = ID;
        capacity = cap;
        currentCapacity = cap;
    }

    // ---------- setters ----------
    public void  setBusID(int ID)          { busID = ID; }
    public void  setRoute(Route r)          { route = r; }
    public void  setBusStatus(BusStatus bs)      { busStatus = bs; }
    public void  setCapacity(int cap)       { capacity=cap; }
    public void  setCurrentCapacity(int cap) { currentCapacity = cap; }
    public void  setLocation(Location lt)       { location = lt; }

    public boolean allocateDriver(Driver d) {
        boolean returnValue = true;

        return returnValue;
    }

    public boolean removePassenger() {
        boolean returnValue;
        if(currentCapacity>0) {
            currentCapacity--;
            returnValue = true;
        } else {
            returnValue = false;
        }
        return returnValue;
    }

    public boolean addPassenger() {
        boolean returnValue;
        if(currentCapacity<capacity) {
            currentCapacity++;
            returnValue = true;
        } else {
            returnValue = false;
        }
        return returnValue;
    }

}
// make function regarding allocation of bus to driver in the driver class