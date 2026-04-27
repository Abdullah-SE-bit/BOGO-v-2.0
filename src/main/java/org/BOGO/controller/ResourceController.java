package org.BOGO.controller;

import org.BOGO.service.HandleDriverService;
import org.BOGO.service.ManageStopsService;
import org.BOGO.service.RouteReviseService;

public class ResourceController {

    private final HandleDriverService  handleDriverService;
    private final RouteReviseService   routeReviseService;
    private final ManageStopsService   manageStopsService;

    public ResourceController(HandleDriverService handleDriverService,
                              RouteReviseService routeReviseService,
                              ManageStopsService manageStopsService) {
        this.handleDriverService = handleDriverService;
        this.routeReviseService  = routeReviseService;
        this.manageStopsService  = manageStopsService;
    }

}
