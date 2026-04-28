package org.BOGO.controller;

import org.BOGO.service.HandleDriverService;
import org.BOGO.service.ManageStopsService;
import org.BOGO.service.RouteReviseService;

public class ResourceController {

    private final HandleDriverService  handleDriverService = new HandleDriverService();
    private final RouteReviseService   routeReviseService = new RouteReviseService();
    private final ManageStopsService   manageStopsService = new ManageStopsService();

}
