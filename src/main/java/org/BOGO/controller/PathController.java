package org.BOGO.controller;

import org.BOGO.service.PathBuildingService;

public class PathController {

    private final PathBuildingService pathBuildingService;

    public PathController(PathBuildingService pathBuildingService) {
        this.pathBuildingService = pathBuildingService;
    }

}
