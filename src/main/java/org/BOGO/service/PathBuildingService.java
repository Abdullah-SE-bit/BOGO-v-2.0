package org.BOGO.service;

import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.*;
import org.BOGO.domain.transport.Map;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;

import java.util.*;

public class PathBuildingService {
    private Map map;

    //------------Constructor-------------
    public PathBuildingService(Map map) {
        this.map = map;
    }

    //----------BFS Shortest Path----------
    public Path buildPath(int sourceID,int destinationID) {

        Stop source = map.getStopById(sourceID);
        Stop destination = map.getStopById(destinationID);

        Path finalPath = new Path();

        if(source==null || destination==null) {
            System.out.println("Stop not found");
            return finalPath;
        }

        int totalStops = map.getStops().size();

        Queue<Stop> queue = new LinkedList<>();

        boolean[] visited = new boolean[totalStops+1];

        Stop[] parent = new Stop[totalStops+1];


        queue.add(source);

        visited[source.getStopID()] = true;


        while(!queue.isEmpty()) {

            Stop current = queue.poll();

            if(current.getStopID()==destinationID) {
                break;
            }

            for(int i=0;i<current.getConnections().size();i++) {

                Stop neighbor = current.getConnections().get(i);

                if(!visited[neighbor.getStopID()]) {

                    visited[neighbor.getStopID()]=true;
                    parent[neighbor.getStopID()]=current;

                    queue.add(neighbor);
                }
            }
        }

        return reconstructPath(source, destination, parent);
    }

    //----------Rebuild Path------------
    private Path reconstructPath(
            Stop source,
            Stop destination,
            Stop[] parent) {

        Path pathObject = new Path();

        ArrayList<Stop> stopPath =
                new ArrayList<>();


        if(source!=destination &&
                parent[destination.getStopID()] == null) {

            System.out.println("No path exists");

            return pathObject;
        }


        Stop current = destination;


        while(current!=null) {

            stopPath.add(current);

            current =
                    parent[current.getStopID()];
        }


        Collections.reverse(stopPath);


        //-----------------------------------
        // Fill Path stops
        //-----------------------------------

        pathObject.setStops(stopPath);



        //-----------------------------------
        // Calculate total fare and routes
        //-----------------------------------

        ArrayList<Route> routeList = new ArrayList<>();
        double totalCost=0;
        int previousRouteID=-1;

        for(int i=0;i<stopPath.size()-1;i++) {

            Stop from = stopPath.get(i);
            Stop to = stopPath.get(i+1);
            int connectionIndex=-1;

            for(int j=0; j<from.getConnections().size(); j++) {

                if(from.getConnections().get(j).getStopID() == to.getStopID()) {
                    connectionIndex=j;
                    break;
                }
            }
            if(connectionIndex!=-1) {

                double fare= from.getConnectionsFair().get(connectionIndex);
                totalCost += fare;
                int routeID= from.getConnectionRoutesId().get(connectionIndex);
                // add route only if changed
                if(routeID!=previousRouteID) {
                    Route route = map.getRouteById(routeID);

                    if(route!=null) {
                        routeList.add(route);
                    }
                    previousRouteID= routeID;
                }
            }
        }

        pathObject.setRoutes(routeList);

        pathObject.setTotalCost(totalCost);


        return pathObject;
    }


    //-----------Display Path------------
    public void displayPath(int source,int destination) {

        Path path = buildPath(source,destination);

        if(path.getStops().isEmpty()) {
            return;
        }

        System.out.println("Path:");

        for(int i=0;i<path.getStops().size();i++) {

            System.out.print(path.getStops().get(i).getStopName());

            if(i != path.getStops().size()-1) {
                System.out.print(" -> ");
            }
        }

        System.out.println();
        System.out.println("Total Cost: " + path.getTotalCost());


        if(!path.getRoutes().isEmpty()) {
            System.out.print("Routes Used: ");
            for(int i=0;i<path.getRoutes().size();i++) {
                System.out.print(path.getRoutes().get(i).getRouteID());
                if(i != path.getRoutes().size()-1) {
                    System.out.print(" -> ");
                }
            }

            System.out.println();
        }
    }

    /**
     * Builds the simplest direct path between two stops on a single route.
     */
    public Path calculatePath(Stop pickup, Stop destination) {
        return null;
    }

    /**
     * Applies multi-leg pathfinding to build an optimal multi-bus transfer path.
     */
    public Path calculateMultiLegPath(Stop pickup, Stop destination) {
        return null;
    }

    /**
     * Recalculates the remaining path from the passenger's current location
     * (used when a transfer stop is missed).
     */
    public Path recalculatePath(Location currentLocation, Stop destination) {
        return null;
    }

    /**
     * Returns all stops within the given path where a bus transfer is required.
     */
    public List<Stop> getTransferPoints(Path path) {
        return null;
    }

    /**
     * Estimates total journey time in minutes, including expected transfer waits.
     */
    public int estimateJourneyTime(Path path) {
        return 0;
    }
}
