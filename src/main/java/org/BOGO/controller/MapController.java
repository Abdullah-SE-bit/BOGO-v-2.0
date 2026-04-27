package org.BOGO.controller;

import org.BOGO.service.ManageStopsService;
import org.BOGO.service.ViewMapService;

public class MapController {

    private final ViewMapService    viewMapService;
    private final ManageStopsService manageStopsService;

    public MapController(ViewMapService viewMapService,
                         ManageStopsService manageStopsService) {
        this.viewMapService     = viewMapService;
        this.manageStopsService = manageStopsService;
    }


}
