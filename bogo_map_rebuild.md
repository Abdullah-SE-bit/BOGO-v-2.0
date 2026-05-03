# BOGO — Map System Complete Rebuild Instructions
## For Antigravity Agent — URGENT (4-Hour Deadline)

---

## ⚠️ CRITICAL BRIEFING — READ FIRST

The existing map system and simulation engine are **broken at the root**. The simulation thread starts but immediately freezes the UI window, making it unresponsive and requiring a force-close. The root causes are:

1. **Simulation thread is blocking the JavaFX Application Thread** — UI calls are not properly deferred
2. **No thread lifecycle management** — thread keeps running even when its target UI is gone, causing deadlocks
3. **Tick speed too slow (2 seconds)** — should be 200–500ms with interpolation for smooth animation
4. **No coordinate normalization** — one outlier stop with a large lat/long value pushes all other stops to one corner of the canvas
5. **Shared canvas with no render mode switching** — all map types are trying to use the same logic

**Your task: DELETE the existing map rendering code and simulation engine entirely. Rebuild from scratch using the architecture in this document. Do NOT try to patch the old code.**

---

## 1. The Three Map Types — Overview

You will build **three completely separate map controllers and canvas renderers**. They share data sources (DB repositories) but have zero shared rendering code.

| Map | Location in UI | Who Sees It | What It Shows | Simulation? |
|---|---|---|---|---|
| **GeneralMap** | Dashboard of Admin, Driver, Passenger | All actors | All stops, all routes, ALL buses moving | Yes — all active trips |
| **BookingMap** | Passenger booking menu | Passenger only | All stops, all routes, highlighted path when selected | No buses, No moving |
| **DriverRouteMap** | Driver "View Route" window | Driver only | Only assigned route stops, ONE bus moving on that route | Yes — one bus only |

---

## 2. Architecture — Non-Negotiable Rules

### Rule 1: Simulation thread NEVER touches JavaFX nodes directly
Every single UI update from the simulation thread MUST go through:
```java
Platform.runLater(() -> { /* UI update here */ });
```
Any direct call to a Canvas, Label, Node, or any JavaFX object from outside the FX thread causes a freeze. This is the root cause of the current bug.

### Rule 2: Each map has its own AnimationTimer (not a background thread)
Do NOT use `ScheduledExecutorService` or `Thread` for map rendering. Use JavaFX's own `AnimationTimer`. It runs on the FX Application Thread automatically — no `Platform.runLater` needed, no freeze possible.

```java
AnimationTimer timer = new AnimationTimer() {
    @Override
    public void handle(long now) {
        // this runs on FX thread — safe to draw on Canvas here
        updateSimulationState(now);
        redrawCanvas();
    }
};
timer.start();
```

### Rule 3: Simulation state lives in a separate plain Java object (not in the UI)
Create a `SimulationState.java` class (plain Java, no JavaFX). This holds the bus positions. The AnimationTimer reads from it and draws. The state updates happen purely in Java math — no JavaFX involved until the draw call.

### Rule 4: Stop/start the timer when the tab is shown/hidden
When a user navigates away from a map tab, STOP the AnimationTimer. When they navigate back, START it again. This prevents phantom threads updating invisible canvases.

```java
// In the FXML controller, listen to tab visibility:
mapPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
    if (newScene == null) animationTimer.stop();
    else animationTimer.start();
});
```

### Rule 5: Always normalize coordinates before drawing
Never use raw lat/long values as pixel coordinates. Always pass all stops through the `CoordinateNormalizer` first. See Section 4.

---

## 3. Package Structure for New Map System

Place all new map code in these packages under `org.BOGO`:

```
org.BOGO
├── map
│   ├── CoordinateNormalizer.java       ← normalization utility (shared)
│   ├── MapDataLoader.java              ← loads stops + routes from DB (shared)
│   ├── SimulationState.java            ← holds bus positions in memory (shared)
│   ├── BusSimulationEngine.java        ← pure Java math engine (no JavaFX)
│   ├── GeneralMapRenderer.java         ← renders general map on Canvas
│   ├── BookingMapRenderer.java         ← renders booking map on Canvas
│   └── DriverRouteMapRenderer.java     ← renders driver route map on Canvas
└── controller
    ├── GeneralMapController.java       ← FXML controller for general map
    ├── BookingMapController.java       ← FXML controller for booking map
    └── DriverRouteMapController.java   ← FXML controller for driver route map
```

---

## 4. CoordinateNormalizer.java — Build This First

This is the most important utility class. It converts raw lat/long into pixel X/Y coordinates that fit within the canvas, regardless of the actual coordinate values.

**Package**: `org.BOGO.map`

### Algorithm: Min-Max Normalization with Padding

```java
package org.BOGO.map;

import org.BOGO.domain.Stop;
import java.util.List;

public class CoordinateNormalizer {

    private double minLat, maxLat, minLon, maxLon;
    private double canvasWidth, canvasHeight;
    private static final double PADDING = 0.10; // 10% padding on all sides

    /**
     * Call this ONCE after loading stops from DB.
     * Pass ALL stops in the system (not just visible ones).
     * This caches the bounds so individual toPixelX/toPixelY calls are O(1).
     */
    public void calibrate(List<Stop> allStops, double canvasWidth, double canvasHeight) {
        if (allStops == null || allStops.isEmpty()) return;

        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        minLat = allStops.stream().mapToDouble(s -> s.getLocation().getLatitude()).min().orElse(0);
        maxLat = allStops.stream().mapToDouble(s -> s.getLocation().getLatitude()).max().orElse(1);
        minLon = allStops.stream().mapToDouble(s -> s.getLocation().getLongitude()).min().orElse(0);
        maxLon = allStops.stream().mapToDouble(s -> s.getLocation().getLongitude()).max().orElse(1);

        // Prevent division by zero if all stops have same coordinate
        if (maxLat == minLat) { maxLat = minLat + 0.001; }
        if (maxLon == minLon) { maxLon = minLon + 0.001; }
    }

    /**
     * Convert a longitude value to a canvas X pixel coordinate.
     */
    public double toPixelX(double longitude) {
        double rangeX = maxLon - minLon;
        double paddingX = rangeX * PADDING;
        return ((longitude - minLon + paddingX) / (rangeX + 2 * paddingX)) * canvasWidth;
    }

    /**
     * Convert a latitude value to a canvas Y pixel coordinate.
     * Note: Y is INVERTED — higher latitude = lower Y on screen.
     */
    public double toPixelY(double latitude) {
        double rangeY = maxLat - minLat;
        double paddingY = rangeY * PADDING;
        // Invert Y axis: higher lat = top of screen
        return (1.0 - ((latitude - minLat + paddingY) / (rangeY + 2 * paddingY))) * canvasHeight;
    }

    /**
     * Interpolate pixel position between two stops.
     * progress = 0.0 means at fromStop, 1.0 means at toStop.
     */
    public double[] interpolatePixel(Stop fromStop, Stop toStop, double progress) {
        double x1 = toPixelX(fromStop.getLocation().getLongitude());
        double y1 = toPixelY(fromStop.getLocation().getLatitude());
        double x2 = toPixelX(toStop.getLocation().getLongitude());
        double y2 = toPixelY(toStop.getLocation().getLatitude());
        return new double[]{
            x1 + (x2 - x1) * progress,
            y1 + (y2 - y1) * progress
        };
    }

    public boolean isCalibrated() {
        return canvasWidth > 0 && canvasHeight > 0;
    }
}
```

**When to call `calibrate()`**: Once when the map loads, and again if the canvas is resized.

---

## 5. MapDataLoader.java

**Package**: `org.BOGO.map`

Loads all map data from the DB in one blocking call. Call this on a background thread at startup, cache the result, and never call it again during rendering.

```java
package org.BOGO.map;

import org.BOGO.domain.*;
import org.BOGO.repository.*;
import java.util.List;

public class MapDataLoader {

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;

    // Cached data — loaded once
    private List<Stop> allStops;
    private List<Route> allRoutes;
    private List<Trip> activeTrips;
    private boolean loaded = false;

    public MapDataLoader(StopRepository stopRepo, RouteRepository routeRepo, TripRepository tripRepo) {
        this.stopRepository = stopRepo;
        this.routeRepository = routeRepo;
        this.tripRepository = tripRepo;
    }

    /**
     * Call this on a background thread (NOT the FX thread).
     * Uses Task<Void> from the FXML controller.
     */
    public void loadAll() {
        this.allStops = stopRepository.findAll();       // loads connections from Connections column
        this.allRoutes = routeRepository.findAll();     // loads ordered stops from Stop_IDs column
        this.activeTrips = tripRepository.findActiveTrips();
        this.loaded = true;
    }

    public List<Stop> getAllStops() { return allStops; }
    public List<Route> getAllRoutes() { return allRoutes; }
    public List<Trip> getActiveTrips() { return activeTrips; }
    public boolean isLoaded() { return loaded; }

    /**
     * Refresh only active trips (lightweight — call periodically if needed).
     * Safe to call from a background thread.
     */
    public void refreshTrips() {
        this.activeTrips = tripRepository.findActiveTrips();
    }
}
```

---

## 6. SimulationState.java

**Package**: `org.BOGO.map`

Pure Java — no JavaFX. Holds the current interpolated position of every bus.

```java
package org.BOGO.map;

import org.BOGO.domain.*;
import java.util.*;

public class SimulationState {

    // tripId -> current progress (0.0 to 1.0 between currentStop and nextStop)
    private final Map<Integer, Double> progressMap = new HashMap<>();

    // tripId -> current stop index in route
    private final Map<Integer, Integer> stopIndexMap = new HashMap<>();

    // tripId -> dwell counter (how many ticks left paused at a stop)
    private final Map<Integer, Integer> dwellMap = new HashMap<>();

    // tripId -> current interpolated pixel position [x, y]
    private final Map<Integer, double[]> pixelPositionMap = new HashMap<>();

    // tripId -> the Trip object
    private final Map<Integer, Trip> tripMap = new HashMap<>();

    public void initializeTrips(List<Trip> trips) {
        for (Trip trip : trips) {
            int id = trip.getTripId();
            tripMap.put(id, trip);
            progressMap.put(id, 0.0);
            stopIndexMap.put(id, 0);
            dwellMap.put(id, 0);
            pixelPositionMap.put(id, new double[]{0, 0});
        }
    }

    public Map<Integer, Trip> getTripMap() { return tripMap; }
    public double getProgress(int tripId) { return progressMap.getOrDefault(tripId, 0.0); }
    public void setProgress(int tripId, double p) { progressMap.put(tripId, p); }
    public int getStopIndex(int tripId) { return stopIndexMap.getOrDefault(tripId, 0); }
    public void setStopIndex(int tripId, int idx) { stopIndexMap.put(tripId, idx); }
    public int getDwell(int tripId) { return dwellMap.getOrDefault(tripId, 0); }
    public void setDwell(int tripId, int d) { dwellMap.put(tripId, d); }
    public double[] getPixelPosition(int tripId) { return pixelPositionMap.getOrDefault(tripId, new double[]{0,0}); }
    public void setPixelPosition(int tripId, double[] pos) { pixelPositionMap.put(tripId, pos); }
    public Set<Integer> getTripIds() { return tripMap.keySet(); }
}
```

---

## 7. BusSimulationEngine.java

**Package**: `org.BOGO.map`

Pure Java math — NO JavaFX, NO Thread, NO ScheduledExecutorService. This is called from an `AnimationTimer.handle()` which already runs on the FX thread safely.

```java
package org.BOGO.map;

import org.BOGO.domain.*;
import java.util.List;

public class BusSimulationEngine {

    private static final double PROGRESS_PER_SECOND = 1.0 / 30.0; // 30 seconds per stop segment
    private static final int DWELL_TICKS = 15;  // pause ~3 seconds at each stop (at 5fps)
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private long lastUpdateNanos = -1;

    private final SimulationState state;
    private final CoordinateNormalizer normalizer;

    public BusSimulationEngine(SimulationState state, CoordinateNormalizer normalizer) {
        this.state = state;
        this.normalizer = normalizer;
    }

    /**
     * Called every frame by AnimationTimer.handle(now).
     * 'now' is the current time in nanoseconds.
     * Returns true if any position changed (caller should redraw canvas).
     */
    public boolean tick(long now) {
        if (lastUpdateNanos < 0) {
            lastUpdateNanos = now;
            return false;
        }

        double deltaSeconds = (now - lastUpdateNanos) / NANOS_PER_SECOND;
        lastUpdateNanos = now;

        // Cap delta to prevent huge jumps after tab switch / pause
        if (deltaSeconds > 0.5) deltaSeconds = 0.5;

        boolean anyChange = false;

        for (int tripId : state.getTripIds()) {
            Trip trip = state.getTripMap().get(tripId);
            Route route = trip.getRoute();
            if (route == null) continue;

            List<Stop> stops = route.getStops();
            if (stops == null || stops.size() < 2) continue;

            int idx = state.getStopIndex(tripId);

            // Handle dwell at stop
            int dwell = state.getDwell(tripId);
            if (dwell > 0) {
                state.setDwell(tripId, dwell - 1);
                continue;
            }

            // Last stop — loop back to beginning
            if (idx >= stops.size() - 1) {
                state.setStopIndex(tripId, 0);
                state.setProgress(tripId, 0.0);
                state.setDwell(tripId, DWELL_TICKS);
                anyChange = true;
                continue;
            }

            // Advance progress
            double progress = state.getProgress(tripId) + (PROGRESS_PER_SECOND * deltaSeconds);

            if (progress >= 1.0) {
                // Arrived at next stop
                int newIdx = idx + 1;
                state.setStopIndex(tripId, newIdx);
                state.setProgress(tripId, 0.0);
                state.setDwell(tripId, DWELL_TICKS);

                // Snap to exact stop pixel
                Stop arrivedStop = stops.get(newIdx);
                state.setPixelPosition(tripId, new double[]{
                    normalizer.toPixelX(arrivedStop.getLocation().getLongitude()),
                    normalizer.toPixelY(arrivedStop.getLocation().getLatitude())
                });
            } else {
                // Interpolate between current stop and next stop
                state.setProgress(tripId, progress);
                Stop from = stops.get(idx);
                Stop to   = stops.get(idx + 1);
                state.setPixelPosition(tripId, normalizer.interpolatePixel(from, to, progress));
            }

            anyChange = true;
        }

        return anyChange;
    }

    public void reset() {
        lastUpdateNanos = -1;
    }
}
```

---

## 8. GeneralMapRenderer.java

**Package**: `org.BOGO.map`

Draws everything onto a JavaFX `Canvas`. Called from `AnimationTimer` — already on FX thread.

```java
package org.BOGO.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.BOGO.domain.*;
import java.util.List;

public class GeneralMapRenderer {

    // Visual constants — adjust to match NeonTheme
    private static final Color BG_COLOR       = Color.web("#0a0a1a");
    private static final Color ROUTE_COLOR     = Color.web("#00aaff");
    private static final Color STOP_COLOR      = Color.web("#ffffff");
    private static final Color STOP_LABEL      = Color.web("#aaaaaa");
    private static final Color BUS_COLOR       = Color.web("#00ff99");
    private static final Color INACTIVE_COLOR  = Color.web("#444444");
    private static final double ROUTE_WIDTH    = 2.0;
    private static final double STOP_RADIUS    = 5.0;
    private static final double BUS_RADIUS     = 8.0;

    private final Canvas canvas;
    private final CoordinateNormalizer normalizer;

    public GeneralMapRenderer(Canvas canvas, CoordinateNormalizer normalizer) {
        this.canvas = canvas;
        this.normalizer = normalizer;
    }

    /**
     * Full redraw. Called every frame by AnimationTimer.
     * Keep this method fast — no DB calls, no allocations beyond what's needed.
     */
    public void draw(List<Stop> stops, List<Route> routes, SimulationState simState) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // 1. Clear canvas
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        if (!normalizer.isCalibrated()) return;

        // 2. Draw route lines
        gc.setLineWidth(ROUTE_WIDTH);
        for (Route route : routes) {
            if (!route.isActive()) {
                gc.setStroke(INACTIVE_COLOR);
            } else {
                gc.setStroke(ROUTE_COLOR);
            }
            List<Stop> routeStops = route.getStops();
            if (routeStops == null || routeStops.size() < 2) continue;

            for (int i = 0; i < routeStops.size() - 1; i++) {
                Stop a = routeStops.get(i);
                Stop b = routeStops.get(i + 1);
                gc.strokeLine(
                    normalizer.toPixelX(a.getLocation().getLongitude()),
                    normalizer.toPixelY(a.getLocation().getLatitude()),
                    normalizer.toPixelX(b.getLocation().getLongitude()),
                    normalizer.toPixelY(b.getLocation().getLatitude())
                );
            }
        }

        // 3. Draw stops
        gc.setFont(Font.font(9));
        for (Stop stop : stops) {
            double x = normalizer.toPixelX(stop.getLocation().getLongitude());
            double y = normalizer.toPixelY(stop.getLocation().getLatitude());

            if (!stop.isActive()) {
                gc.setFill(INACTIVE_COLOR);
            } else {
                gc.setFill(STOP_COLOR);
            }
            gc.fillOval(x - STOP_RADIUS, y - STOP_RADIUS, STOP_RADIUS * 2, STOP_RADIUS * 2);

            // Label
            gc.setFill(STOP_LABEL);
            gc.fillText(stop.getStopName(), x + STOP_RADIUS + 2, y + 4);
        }

        // 4. Draw buses
        if (simState != null) {
            gc.setFill(BUS_COLOR);
            for (int tripId : simState.getTripIds()) {
                double[] pos = simState.getPixelPosition(tripId);
                if (pos == null) continue;
                // Draw bus as filled circle with outline
                gc.setFill(BUS_COLOR);
                gc.fillOval(pos[0] - BUS_RADIUS, pos[1] - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeOval(pos[0] - BUS_RADIUS, pos[1] - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
                // Bus label
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font(8));
                gc.fillText("B", pos[0] - 3, pos[1] + 3);
            }
        }
    }
}
```

---

## 9. BookingMapRenderer.java

**Package**: `org.BOGO.map`

No simulation. No buses. Draws routes and stops. When a path is selected by the passenger, highlights it.

```java
package org.BOGO.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.BOGO.domain.*;
import java.util.List;

public class BookingMapRenderer {

    private static final Color BG_COLOR         = Color.web("#0a0a1a");
    private static final Color ROUTE_COLOR       = Color.web("#334455");      // dim — unselected
    private static final Color HIGHLIGHTED_ROUTE = Color.web("#00ff99");      // bright — selected path
    private static final Color STOP_COLOR        = Color.web("#aaaaaa");
    private static final Color SELECTED_STOP     = Color.web("#ffff00");      // origin/destination
    private static final Color STOP_LABEL        = Color.web("#888888");
    private static final double ROUTE_WIDTH      = 1.5;
    private static final double HIGHLIGHT_WIDTH  = 3.5;
    private static final double STOP_RADIUS      = 4.0;

    private final Canvas canvas;
    private final CoordinateNormalizer normalizer;

    // Set by controller when passenger selects a path
    private Path selectedPath = null;
    private Stop originStop   = null;
    private Stop destStop     = null;

    public BookingMapRenderer(Canvas canvas, CoordinateNormalizer normalizer) {
        this.canvas = canvas;
        this.normalizer = normalizer;
    }

    public void setSelectedPath(Path path, Stop origin, Stop destination) {
        this.selectedPath = path;
        this.originStop   = origin;
        this.destStop     = destination;
    }

    public void clearSelection() {
        this.selectedPath = null;
        this.originStop   = null;
        this.destStop     = null;
    }

    /**
     * Static draw — no AnimationTimer needed.
     * Call this after: (a) initial load, (b) path selection changes.
     */
    public void draw(List<Stop> stops, List<Route> routes) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        if (!normalizer.isCalibrated()) return;

        // 1. Draw all routes (dim)
        gc.setStroke(ROUTE_COLOR);
        gc.setLineWidth(ROUTE_WIDTH);
        for (Route route : routes) {
            List<Stop> rs = route.getStops();
            if (rs == null || rs.size() < 2) continue;
            for (int i = 0; i < rs.size() - 1; i++) {
                gc.strokeLine(
                    normalizer.toPixelX(rs.get(i).getLocation().getLongitude()),
                    normalizer.toPixelY(rs.get(i).getLocation().getLatitude()),
                    normalizer.toPixelX(rs.get(i+1).getLocation().getLongitude()),
                    normalizer.toPixelY(rs.get(i+1).getLocation().getLatitude())
                );
            }
        }

        // 2. Draw highlighted path on top (if selected)
        if (selectedPath != null) {
            gc.setStroke(HIGHLIGHTED_ROUTE);
            gc.setLineWidth(HIGHLIGHT_WIDTH);
            List<Stop> pathStops = selectedPath.getStops();
            if (pathStops != null && pathStops.size() >= 2) {
                for (int i = 0; i < pathStops.size() - 1; i++) {
                    gc.strokeLine(
                        normalizer.toPixelX(pathStops.get(i).getLocation().getLongitude()),
                        normalizer.toPixelY(pathStops.get(i).getLocation().getLatitude()),
                        normalizer.toPixelX(pathStops.get(i+1).getLocation().getLongitude()),
                        normalizer.toPixelY(pathStops.get(i+1).getLocation().getLatitude())
                    );
                }
            }
        }

        // 3. Draw all stops
        gc.setFont(Font.font(9));
        for (Stop stop : stops) {
            double x = normalizer.toPixelX(stop.getLocation().getLongitude());
            double y = normalizer.toPixelY(stop.getLocation().getLatitude());

            boolean isEndpoint = (stop.equals(originStop) || stop.equals(destStop));
            gc.setFill(isEndpoint ? SELECTED_STOP : STOP_COLOR);
            gc.fillOval(x - STOP_RADIUS, y - STOP_RADIUS, STOP_RADIUS * 2, STOP_RADIUS * 2);

            gc.setFill(STOP_LABEL);
            gc.fillText(stop.getStopName(), x + STOP_RADIUS + 2, y + 4);
        }
    }
}
```

---

## 10. DriverRouteMapRenderer.java

**Package**: `org.BOGO.map`

Shows ONLY the driver's assigned route and ONE bus moving on it.

```java
package org.BOGO.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.BOGO.domain.*;
import java.util.List;

public class DriverRouteMapRenderer {

    private static final Color BG_COLOR      = Color.web("#0a0a1a");
    private static final Color ROUTE_COLOR   = Color.web("#0088ff");
    private static final Color STOP_COLOR    = Color.web("#ffffff");
    private static final Color BLOCKED_STOP  = Color.web("#ff4444");
    private static final Color BUS_COLOR     = Color.web("#00ff99");
    private static final Color LABEL_COLOR   = Color.web("#aaaaaa");
    private static final double ROUTE_WIDTH  = 3.0;
    private static final double STOP_RADIUS  = 6.0;
    private static final double BUS_RADIUS   = 9.0;

    private final Canvas canvas;
    private final CoordinateNormalizer normalizer;

    // The single bus position for this driver
    private double busX = 0, busY = 0;

    public DriverRouteMapRenderer(Canvas canvas, CoordinateNormalizer normalizer) {
        this.canvas = canvas;
        this.normalizer = normalizer;
    }

    public void updateBusPosition(double x, double y) {
        this.busX = x;
        this.busY = y;
    }

    /**
     * Draw the driver's route with one bus moving on it.
     * Called every frame by AnimationTimer.
     */
    public void draw(Route driverRoute) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        if (!normalizer.isCalibrated() || driverRoute == null) return;

        List<Stop> stops = driverRoute.getStops();
        if (stops == null || stops.size() < 2) return;

        // 1. Draw route line
        gc.setStroke(ROUTE_COLOR);
        gc.setLineWidth(ROUTE_WIDTH);
        for (int i = 0; i < stops.size() - 1; i++) {
            gc.strokeLine(
                normalizer.toPixelX(stops.get(i).getLocation().getLongitude()),
                normalizer.toPixelY(stops.get(i).getLocation().getLatitude()),
                normalizer.toPixelX(stops.get(i+1).getLocation().getLongitude()),
                normalizer.toPixelY(stops.get(i+1).getLocation().getLatitude())
            );
        }

        // 2. Draw stops with tooltips handled by controller
        gc.setFont(Font.font(10));
        for (Stop stop : stops) {
            double x = normalizer.toPixelX(stop.getLocation().getLongitude());
            double y = normalizer.toPixelY(stop.getLocation().getLatitude());

            gc.setFill(stop.isActive() ? STOP_COLOR : BLOCKED_STOP);
            gc.fillOval(x - STOP_RADIUS, y - STOP_RADIUS, STOP_RADIUS * 2, STOP_RADIUS * 2);

            gc.setFill(LABEL_COLOR);
            gc.fillText(stop.getStopName(), x + STOP_RADIUS + 2, y + 4);

            // Draw "BLOCKED" label
            if (!stop.isActive()) {
                gc.setFill(BLOCKED_STOP);
                gc.fillText("BLOCKED", x + STOP_RADIUS + 2, y + 14);
            }
        }

        // 3. Draw bus
        gc.setFill(BUS_COLOR);
        gc.fillOval(busX - BUS_RADIUS, busY - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(busX - BUS_RADIUS, busY - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
    }
}
```

---

## 11. GeneralMapController.java — Complete FXML Controller

**Package**: `org.BOGO.controller`

This is the controller for the General Map. It wires together the AnimationTimer, the renderer, and the simulation engine with safe threading.

```java
package org.BOGO.controller;

import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import org.BOGO.map.*;
import org.BOGO.domain.*;
import org.BOGO.repository.*;

public class GeneralMapController {

    @FXML private Canvas mapCanvas;
    @FXML private Label statusLabel;

    private final StopRepository stopRepo;
    private final RouteRepository routeRepo;
    private final TripRepository tripRepo;

    private CoordinateNormalizer normalizer;
    private SimulationState simState;
    private BusSimulationEngine engine;
    private GeneralMapRenderer renderer;
    private MapDataLoader dataLoader;
    private AnimationTimer animationTimer;

    public GeneralMapController(StopRepository stopRepo, RouteRepository routeRepo, TripRepository tripRepo) {
        this.stopRepo  = stopRepo;
        this.routeRepo = routeRepo;
        this.tripRepo  = tripRepo;
    }

    @FXML
    public void initialize() {
        statusLabel.setText("Loading map...");

        // Step 1: Load all data on a BACKGROUND THREAD (never block FX thread)
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                dataLoader = new MapDataLoader(stopRepo, routeRepo, tripRepo);
                dataLoader.loadAll();
                return null;
            }
        };

        // Step 2: After data loaded, set up simulation on FX thread
        loadTask.setOnSucceeded(e -> {
            setupSimulation();
            statusLabel.setText("");
        });

        loadTask.setOnFailed(e -> {
            statusLabel.setText("Failed to load map data.");
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);  // CRITICAL: daemon thread dies with app
        thread.start();

        // Step 3: Stop animation when this pane is removed from scene
        mapCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && animationTimer != null) {
                animationTimer.stop();
            } else if (newScene != null && animationTimer != null) {
                animationTimer.start();
            }
        });
    }

    private void setupSimulation() {
        normalizer = new CoordinateNormalizer();
        normalizer.calibrate(dataLoader.getAllStops(), mapCanvas.getWidth(), mapCanvas.getHeight());

        // Re-calibrate if canvas is resized
        mapCanvas.widthProperty().addListener((obs, o, n) ->
            normalizer.calibrate(dataLoader.getAllStops(), n.doubleValue(), mapCanvas.getHeight()));
        mapCanvas.heightProperty().addListener((obs, o, n) ->
            normalizer.calibrate(dataLoader.getAllStops(), mapCanvas.getWidth(), n.doubleValue()));

        simState = new SimulationState();
        simState.initializeTrips(dataLoader.getActiveTrips());

        engine   = new BusSimulationEngine(simState, normalizer);
        renderer = new GeneralMapRenderer(mapCanvas, normalizer);

        // AnimationTimer — runs on FX thread, safe for canvas drawing
        animationTimer = new AnimationTimer() {
            private long lastFrameNanos = 0;
            private static final long FRAME_INTERVAL = 100_000_000L; // ~10fps — enough for smooth look

            @Override
            public void handle(long now) {
                if (now - lastFrameNanos < FRAME_INTERVAL) return; // throttle
                lastFrameNanos = now;
                engine.tick(now);
                renderer.draw(dataLoader.getAllStops(), dataLoader.getAllRoutes(), simState);
            }
        };
        animationTimer.start();
    }

    /** Call this when the user navigates away from this map tab */
    public void onHide() {
        if (animationTimer != null) animationTimer.stop();
    }

    /** Call this when the user navigates back to this map tab */
    public void onShow() {
        if (animationTimer != null) animationTimer.start();
        if (engine != null) engine.reset(); // reset delta timer to prevent jump
    }
}
```

---

## 12. BookingMapController.java

**Package**: `org.BOGO.controller`

No AnimationTimer — static map. Redraws only when selection changes.

```java
package org.BOGO.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import org.BOGO.map.*;
import org.BOGO.domain.*;
import org.BOGO.repository.*;
import org.BOGO.service.PathBuildingService;
import java.util.List;

public class BookingMapController {

    @FXML private Canvas mapCanvas;
    @FXML private ComboBox<Stop> originComboBox;
    @FXML private ComboBox<Stop> destComboBox;
    @FXML private Button findPathButton;
    @FXML private Label statusLabel;
    @FXML private Label pathInfoLabel;

    private final StopRepository stopRepo;
    private final RouteRepository routeRepo;
    private final PathBuildingService pathService;

    private CoordinateNormalizer normalizer;
    private BookingMapRenderer renderer;
    private List<Stop> allStops;
    private List<Route> allRoutes;

    public BookingMapController(StopRepository stopRepo, RouteRepository routeRepo,
                                 PathBuildingService pathService) {
        this.stopRepo    = stopRepo;
        this.routeRepo   = routeRepo;
        this.pathService = pathService;
    }

    @FXML
    public void initialize() {
        statusLabel.setText("Loading...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                allStops  = stopRepo.findAll();
                allRoutes = routeRepo.findAll();
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            normalizer = new CoordinateNormalizer();
            normalizer.calibrate(allStops, mapCanvas.getWidth(), mapCanvas.getHeight());

            renderer = new BookingMapRenderer(mapCanvas, normalizer);
            renderer.draw(allStops, allRoutes);  // initial static draw

            // Populate stop dropdowns
            originComboBox.getItems().addAll(allStops);
            destComboBox.getItems().addAll(allStops);

            statusLabel.setText("Select origin and destination stops.");
        });

        Thread t = new Thread(loadTask);
        t.setDaemon(true);
        t.start();

        findPathButton.setOnAction(e -> onFindPath());
    }

    private void onFindPath() {
        Stop origin = originComboBox.getValue();
        Stop dest   = destComboBox.getValue();

        if (origin == null || dest == null) {
            statusLabel.setText("Please select both origin and destination.");
            return;
        }
        if (origin.equals(dest)) {
            statusLabel.setText("Origin and destination cannot be the same.");
            return;
        }

        statusLabel.setText("Finding path...");

        Task<Path> pathTask = new Task<>() {
            @Override
            protected Path call() {
                return pathService.buildPath(origin.getStopId(), dest.getStopId());
            }
        };

        pathTask.setOnSucceeded(e -> {
            Path path = pathTask.getValue();
            if (path == null || path.getStops().isEmpty()) {
                statusLabel.setText("No path found between selected stops.");
                renderer.clearSelection();
            } else {
                renderer.setSelectedPath(path, origin, dest);
                pathInfoLabel.setText("Path: " + path.getStops().size() + " stops | Est. " +
                    path.getTotalEstimatedTime() + "s travel time");
                statusLabel.setText("Path found.");
            }
            renderer.draw(allStops, allRoutes); // redraw with highlight
        });

        pathTask.setOnFailed(e -> statusLabel.setText("Error finding path."));

        Thread t = new Thread(pathTask);
        t.setDaemon(true);
        t.start();
    }
}
```

---

## 13. DriverRouteMapController.java

**Package**: `org.BOGO.controller`

One bus, one route, AnimationTimer active.

```java
package org.BOGO.controller;

import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import org.BOGO.domain.*;
import org.BOGO.map.*;
import org.BOGO.repository.*;

import java.util.ArrayList;
import java.util.List;

public class DriverRouteMapController {

    @FXML private Canvas mapCanvas;
    @FXML private Label statusLabel;

    private final TripRepository tripRepo;
    private final int driverId;

    private CoordinateNormalizer normalizer;
    private SimulationState simState;
    private BusSimulationEngine engine;
    private DriverRouteMapRenderer renderer;
    private AnimationTimer animationTimer;
    private Route driverRoute;

    public DriverRouteMapController(TripRepository tripRepo, int driverId) {
        this.tripRepo = tripRepo;
        this.driverId = driverId;
    }

    @FXML
    public void initialize() {
        statusLabel.setText("Loading your route...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                Trip driverTrip = tripRepo.findActiveByDriverId(driverId);
                if (driverTrip != null) {
                    driverRoute = driverTrip.getRoute();
                }
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            if (driverRoute == null) {
                statusLabel.setText("No active route assigned.");
                return;
            }

            List<Stop> routeStops = driverRoute.getStops();

            normalizer = new CoordinateNormalizer();
            normalizer.calibrate(routeStops, mapCanvas.getWidth(), mapCanvas.getHeight());

            // Create single-trip simulation for driver's bus only
            Trip driverTrip = tripRepo.findActiveByDriverId(driverId);
            simState = new SimulationState();
            simState.initializeTrips(driverTrip != null ? List.of(driverTrip) : new ArrayList<>());

            engine   = new BusSimulationEngine(simState, normalizer);
            renderer = new DriverRouteMapRenderer(mapCanvas, normalizer);

            animationTimer = new AnimationTimer() {
                private long lastFrameNanos = 0;
                private static final long FRAME_INTERVAL = 100_000_000L;

                @Override
                public void handle(long now) {
                    if (now - lastFrameNanos < FRAME_INTERVAL) return;
                    lastFrameNanos = now;
                    engine.tick(now);

                    // Update bus position from simState
                    if (!simState.getTripIds().isEmpty()) {
                        int tripId = simState.getTripIds().iterator().next();
                        double[] pos = simState.getPixelPosition(tripId);
                        renderer.updateBusPosition(pos[0], pos[1]);
                    }
                    renderer.draw(driverRoute);
                }
            };

            animationTimer.start();
            statusLabel.setText("");
        });

        loadTask.setOnFailed(e -> statusLabel.setText("Error loading route."));

        mapCanvas.sceneProperty().addListener((obs, old, newScene) -> {
            if (newScene == null && animationTimer != null) animationTimer.stop();
            else if (newScene != null && animationTimer != null) animationTimer.start();
        });

        Thread t = new Thread(loadTask);
        t.setDaemon(true);
        t.start();
    }

    public void onHide() { if (animationTimer != null) animationTimer.stop(); }
    public void onShow() {
        if (animationTimer != null) animationTimer.start();
        if (engine != null) engine.reset();
    }
}
```

---

## 14. TripRepository — Required Method

The controllers above need `findActiveByDriverId`. Add this if not present:

```java
// In TripRepository.java
public Trip findActiveByDriverId(int driverId) {
    String sql = "SELECT * FROM TRIP WHERE DriverId = ? AND TripStatus = 'IN_PROGRESS'";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setInt(1, driverId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRowToTrip(rs); // your existing mapping method
        }
    } catch (SQLException e) {
        System.err.println("[TripRepository] findActiveByDriverId error: " + e.getMessage());
    }
    return null;
}

public List<Trip> findActiveTrips() {
    String sql = "SELECT * FROM TRIP WHERE TripStatus = 'IN_PROGRESS'";
    List<Trip> trips = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            trips.add(mapRowToTrip(rs));
        }
    } catch (SQLException e) {
        System.err.println("[TripRepository] findActiveTrips error: " + e.getMessage());
    }
    return trips;
}
```

---

## 15. Wiring Into Existing FXML

### In AdminShell FXML controller:
When the dashboard tab becomes visible, call `generalMapController.onShow()`.
When user leaves dashboard, call `generalMapController.onHide()`.

### Canvas FXML declaration (in the relevant FXML files):
```xml
<Canvas fx:id="mapCanvas" width="600" height="400"
        AnchorPane.topAnchor="0" AnchorPane.bottomAnchor="0"
        AnchorPane.leftAnchor="0" AnchorPane.rightAnchor="0"/>
```
Make the canvas resize-aware by binding width/height to parent:
```java
mapCanvas.widthProperty().bind(parentPane.widthProperty());
mapCanvas.heightProperty().bind(parentPane.heightProperty());
// Then re-calibrate normalizer in a listener on those properties
```

---

## 16. Execution Order for This Rebuild

1. Delete existing map rendering code and simulation engine classes entirely
2. Create `CoordinateNormalizer.java` — test with hardcoded stops first
3. Create `SimulationState.java`
4. Create `BusSimulationEngine.java`
5. Create `MapDataLoader.java`
6. Create `GeneralMapRenderer.java`
7. Create `GeneralMapController.java` — wire to dashboard FXML canvas
8. Test: general map loads, buses animate, no freeze ← **verify before continuing**
9. Create `BookingMapRenderer.java` + `BookingMapController.java`
10. Create `DriverRouteMapRenderer.java` + `DriverRouteMapController.java`
11. Add `findActiveByDriverId` and `findActiveTrips` to `TripRepository` if missing
12. Run ALTER TABLE statements for `TripStatus` column if not done yet
13. Add seed trips to DB if none exist (two trips in IN_PROGRESS status)

---

## 17. Common Mistakes to Avoid

| Mistake | Why It Kills the App | Fix |
|---|---|---|
| Calling `gc.fillRect()` from a `ScheduledExecutorService` thread | JavaFX crash / freeze | Use `AnimationTimer` — it's already on FX thread |
| `Platform.runLater()` inside `AnimationTimer.handle()` | Double FX thread dispatch — redundant and causes queuing | Remove `Platform.runLater` inside AnimationTimer |
| Not setting thread as daemon | Thread keeps running after app closes, hangs JVM | Always `thread.setDaemon(true)` |
| Drawing to Canvas while it has zero width/height | Everything draws at (0,0) | Check `canvas.getWidth() > 0` before drawing |
| Using raw lat/long as pixel coords | One outlier stop breaks the whole layout | Always go through `CoordinateNormalizer` |
| Calling DB inside `AnimationTimer.handle()` | DB call every 100ms = 10 DB calls/second = certain freeze | Load data once in `Task`, cache it |
| Not resetting `lastUpdateNanos` after tab switch | First tick after resume has huge delta, buses jump | Call `engine.reset()` in `onShow()` |
