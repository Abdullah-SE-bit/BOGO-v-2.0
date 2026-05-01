package org.BOGO.domain.transport;
import org.BOGO.domain.user.*;


public class Bus {

    private int busID;

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
    public void  setBusStatus(BusStatus bs)      { busStatus = bs; }
    public void  setCapacity(int cap)       { capacity=cap; }
    public void  setCurrentCapacity(int cap) { currentCapacity = cap; }
    public void  setLocation(Location lt)       { location = lt; }

    // ---------- getters ----------
    public int getBusID()          { return busID; }
    public BusStatus getBusStatus()      { return busStatus; }
    public int getCapacity()       { return capacity; }
    public int getCurrentCapacity() { return currentCapacity; }
    public Location getLocation()       { return location; }

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