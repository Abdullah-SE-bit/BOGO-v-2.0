package org.BOGO.domain.booking;

import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import java.util.ArrayList;
import java.util.List;

public class Path {

    private int pathID;

    private ArrayList<Stop> stops = new ArrayList<>();

    private ArrayList<Route> routes = new ArrayList<>();

    private double totalCost;

    // ---------- Constructors ----------
    public Path() {}

    public int getPathID() {return this.pathID;}

    public int getPathID(int pathID) {return this.pathID;}

    public void setPathID(int pathID) { this.pathID = pathID; }

    public ArrayList<Stop> getStops() {
        return stops;
    }

    public void setStops(ArrayList<Stop> stops) {
        this.stops = stops;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

}
