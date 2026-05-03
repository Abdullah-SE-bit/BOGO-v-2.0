# BOGO — Antigravity Agent Build Instructions
## Complete Step-by-Step Instructions for Claude Sonnet (Antigravity)

---

## ⚠️ CRITICAL BRIEFING — READ BEFORE ANYTHING ELSE

You are **Antigravity Claude Sonnet**. You have been given this document and the project's existing source files to build the **BOGO Smart Public Bus Transport System**. You have **24 hours**. This document is your single source of truth.

**Your mandate:**
1. Read every existing source file in the project. Understand what is already built.
2. Follow the instructions in this document exactly.
3. Build a **complete, working, robust, error-free system**.
4. The system must **run** — not just compile.

---

## 1. What You Have Been Given

- All existing source files (UI FXML, partial backend, partial DB schema)
- `schema.sql` — the current (incomplete) database schema
- The full non-technical context document
- The full technical context document
- These instructions

---

## 2. Absolute Rules — These Cannot Be Violated

### Rule 1: UI Files Are Sacred
- **DO NOT delete any existing `.fxml` file.**
- **DO NOT delete any existing CSS rules from `NeonTheme.css`.**
- You MAY **add** new FXML pages.
- You MAY **add** new nav buttons to existing shells (AdminShell.fxml).
- You MAY **add** new CSS rules to `NeonTheme.css`.
- All additions must **visually match** the existing NeonTheme aesthetic exactly.

### Rule 2: The Stop/Route Graph Is Reusable
- The existing stop-route map graph code should be reused as the foundation.
- Build the 4 map variants (General, Booking, Admin Marking, Driver Route) on top of this existing graph code.

### Rule 3: Three Separate JARs
- The final deliverable is **three executable JAR files**: `bogo-admin.jar`, `bogo-driver.jar`, `bogo-passenger.jar`.
- Each targets a specific actor and launches directly into their UI.

### Rule 4: No Static System
- The system must simulate real movement. Buses must appear to move on the map. See Section 10 for full simulation spec.

### Rule 5: Robustness
- Every service method must have a try-catch.
- Every DB operation must handle SQL exceptions gracefully.
- Every UI interaction that triggers a backend call must show a loading indicator and handle failure gracefully (error dialog or status label).

---

## 3. Your First Actions — Read Before You Write

Before writing a single line of code:

1. **Read ALL existing source files** — every `.java`, `.fxml`, `.css`, and `schema.sql` file.
2. **Map what exists vs. what is missing** using Section 4 of this document.
3. **Choose your database strategy** — you may keep SQL Server or switch to another relational DB (PostgreSQL, MySQL, H2 for embedded). Whatever you choose, update `DatabaseConfig.java` accordingly. Justify your choice in a comment at the top of `DatabaseConfig.java`.
4. **Design your full `schema.sql`** before writing any repository code. Add the new fields (tyreHealth, engineHealth, chassisHealth) from day one.

---

## 4. What Exists vs. What to Build

### ✅ Already Exists (Review and reuse or adapt)
- Login / Authentication UI and `AuthService` logic (connected to DB — working)
- General map base rendering (canvas exists — build variants on top)
- Admin Shell (`AdminShell.fxml`) — has nav panel and two existing resource boxes (Driver, Passenger)
- Driver Shell with Send Alert UI
- Passenger Shell with booking map UI
- Driver/Passenger sign-up flow

### ❌ Does Not Exist — You Must Build These

| Component | Type | Priority |
|---|---|---|
| `MapController.java` | Controller | 🔴 Critical |
| `MapRepository.java` | Repository | 🔴 Critical |
| `PathRepository.java` | Repository | 🔴 Critical |
| `PathBuildingService.java` | Service | 🔴 Critical |
| `AddStopsRoutes.fxml` | UI Page | 🔴 Critical |
| `AssignRoute.fxml` | UI Page | 🔴 Critical |
| Driver Route Map (map variant) | UI Feature | 🔴 Critical |
| Admin Marking Map (map variant) | UI Feature | 🔴 Critical |
| General Map with live bus icons | UI Feature | 🔴 Critical |
| Bus Simulation Engine | Background Thread | 🔴 Critical |
| Stop Rerouting Algorithm | Service Logic | 🔴 Critical |
| "Assign Route" nav button in AdminShell | UI Addition | 🟡 High |
| "Add Stops/Routes" nav button in AdminShell | UI Addition | 🟡 High |
| Third box (Buses) in System Analytics UI | UI Addition | 🟡 High |
| Bus Metrics fields in DB + BusRepository | DB + Repo | 🟡 High |
| Real-time bus location update logic | Service Logic | 🟡 High |
| `TripService.java` wiring | Service | 🟡 High |
| Full `ManageStopsService.java` | Service | 🟡 High |

---

## 5. Package Structure to Follow

Create all classes in this exact package hierarchy:

```
com.bogo
├── config
│   └── DatabaseConfig.java
├── domain
│   ├── PersonalDetails.java
│   ├── User.java
│   ├── Admin.java          (extends User)
│   ├── Passenger.java      (extends User)
│   ├── Driver.java         (extends User)
│   ├── Bus.java
│   ├── Location.java
│   ├── Stop.java
│   ├── Route.java
│   ├── Map.java            (Singleton)
│   ├── Path.java
│   ├── Booking.java
│   ├── Alert.java
│   ├── Resolution.java
│   ├── Notification.java
│   └── Trip.java
├── repository
│   ├── UserRepository.java
│   ├── BusRepository.java
│   ├── RouteRepository.java
│   ├── StopRepository.java
│   ├── BookingRepository.java
│   ├── AlertRepository.java
│   ├── NotificationRepository.java
│   ├── TripRepository.java
│   ├── MapRepository.java      ← BUILD THIS
│   └── PathRepository.java     ← BUILD THIS
├── service
│   ├── AuthService.java
│   ├── UserSignUpService.java
│   ├── HandleDriverService.java
│   ├── BusService.java
│   ├── RouteReviseService.java
│   ├── ManageStopsService.java
│   ├── BookingService.java
│   ├── CancellationService.java
│   ├── PathBuildingService.java
│   ├── IssueReportingService.java
│   ├── IssueResolvingService.java
│   ├── NotificationService.java
│   ├── ViewMapService.java
│   └── TripService.java
├── controller
│   ├── BookingController.java
│   ├── MessageController.java
│   ├── ResourceController.java
│   ├── UserController.java
│   └── MapController.java      ← BUILD THIS
└── ui
    ├── admin
    │   ├── AdminApp.java
    │   └── AdminUIController.java
    ├── driver
    │   ├── DriverApp.java
    │   └── DriverUIController.java
    └── passenger
        ├── PassengerApp.java
        └── PassengerUIController.java
```

---

## 6. Database Instructions

### 6.1 Database Engine — SQL Server ONLY
The project uses **Microsoft SQL Server**. Do NOT switch databases. Keep SQL Server.
- JDBC Driver: `com.microsoft.sqlserver:mssql-jdbc` (add to Maven/Gradle)
- Connection string: `jdbc:sqlserver://<host>:<port>;databaseName=bogo;user=<user>;password=<pass>`
- Use `IDENTITY(1,1)` for auto-increment (not `AUTO_INCREMENT`)
- Use `GETDATE()` for current timestamp (not `NOW()`)
- Use `NVARCHAR` for text fields that may contain Unicode
- Do NOT use an ORM — keep plain JDBC

### 6.2 The Existing Schema — What Already Exists

The following tables **already exist** in the database. Do NOT drop and recreate them unless you are rebuilding the entire DB from scratch. Study them carefully — your repository layer MUST map to these exact table and column names.

```
PERSONAL_DETAILS  (PdId, Name, Email, Password, CNIC)
USERS             (UserId, PdId → PERSONAL_DETAILS)
ADMIN             (UserId → USERS)          ← table-per-type inheritance
PASSENGER         (UserId → USERS)          ← table-per-type inheritance
DRIVER            (UserId → USERS, DriverID CHAR(13))  ← table-per-type inheritance
LOCATIONN         (LocationId, Longitude, Latitude)    ← NOTE: double N in table name
STOPS             (StopId, StopName, LocationId → LOCATIONN, Connections NVARCHAR(MAX))
ROUTEE            (RouteId, Active BIT, Stop_IDs NVARCHAR(MAX))  ← NOTE: double E
BUS               (BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity)
TRIP              (TripId, RouteId, BusId, DriverId, DepartureTime, ArrivalTime)
BOOKING           (BookingID, PassengerID, BusID, Active BIT, Cost FLOAT, BookingTime)
ALERTS            (AlertId, SenderDriverId, AlertType, Priority, Message, Status, SentTime)
RESOLUTIONS       (ResolutionId, AlertId, AdminId, NewBusId, NewDriverId, NewRouteId, ResolutionNotes, ResolvedAt)
NOTIFICATIONS     (NotificationId, RecipientUserId, Content, IsRead BIT, CreatedAt)
```

**⚠️ CRITICAL DESIGN NOTES about existing schema:**

1. **`STOPS.Connections`** is stored as `NVARCHAR(MAX)` — this is a **serialized comma-separated string of connected StopIds** (e.g., `"2,5,7"`). When reading stops from the DB, you must parse this string to reconstruct the adjacency list. When writing, serialize the list back to a comma-separated string. Your `StopRepository` must handle this serialization/deserialization.

2. **`ROUTEE.Stop_IDs`** is stored as `NVARCHAR(MAX)` — this is a **serialized ordered comma-separated string of StopIds** that make up the route (e.g., `"1,3,5,7,9"`). Same handling required.

3. **Role inheritance is table-per-type**: To determine if a `UserId` is an Admin, query `SELECT UserId FROM ADMIN WHERE UserId = ?`. The `USERS` table alone does not tell you the role — you must JOIN or query the subtype tables.

4. **Password is in `PERSONAL_DETAILS`**, not in `USERS`. The `Password` column is in `PERSONAL_DETAILS`. Authentication queries must JOIN `USERS` → `PERSONAL_DETAILS` to retrieve and compare the password.

5. **`BOOKING` links to `BusId` directly** — there is no `PathId` in the current `BOOKING` table. After you add the `PATHS` and `PATH_STOPS` tables (see Section 6.4), you must also `ALTER TABLE BOOKING` to add `PathId`.

6. **`DRIVER` has no `assignedRouteId` or `assignedBusId`** — these fields do not exist yet. You must add them via `ALTER TABLE` (see Section 6.4).

7. **`BUS` has no health metrics, no location fields** — these fields do not exist yet. Add via `ALTER TABLE`.

8. **`TRIP` has no simulation fields** — `CurrentStopIndex` and `SimulatedProgress` do not exist yet. Add via `ALTER TABLE`.

9. **Seed data is already inserted**: Admin (`bogo.admin@gmail.com` / `AdminPass123`), Passenger (`john.bogo@gmail.com` / `SecurePass123`), Driver (`driver.bogo@gmail.com` / `DriveSafe2024`, DriverID: `DRI-5566-2024`). Do NOT re-insert these.

### 6.3 Repository Mapping to Actual Table Names

Your Java repository classes must use the EXACT SQL Server table names as they exist:

| Java Class | SQL Server Table | Notes |
|---|---|---|
| `UserRepository` | `USERS`, `PERSONAL_DETAILS`, `ADMIN`, `PASSENGER`, `DRIVER` | Must JOIN across these tables |
| `BusRepository` | `BUS` | |
| `RouteRepository` | `ROUTEE` | Note double E |
| `StopRepository` | `STOPS`, `LOCATIONN` | Note double N on LOCATIONN |
| `BookingRepository` | `BOOKING` | |
| `AlertRepository` | `ALERTS` | |
| `NotificationRepository` | `NOTIFICATIONS` | |
| `TripRepository` | `TRIP` | |
| `MapRepository` | `MAP` (NEW — see 6.4) | Build this table |
| `PathRepository` | `PATHS`, `PATH_STOPS` (NEW — see 6.4) | Build these tables |

### 6.4 ALTER Statements and New Tables — Run These on Top of Existing Schema

Run the following SQL to extend the existing schema with required missing structures:

```sql
-- ============================================================
-- EXTEND EXISTING TABLES WITH MISSING FIELDS
-- ============================================================

-- Add route and bus assignment to DRIVER
ALTER TABLE DRIVER ADD AssignedRouteId INT NULL;
ALTER TABLE DRIVER ADD AssignedBusId INT NULL;
ALTER TABLE DRIVER ADD CONSTRAINT FK_DRIVER_ROUTE 
    FOREIGN KEY (AssignedRouteId) REFERENCES ROUTEE(RouteId);
ALTER TABLE DRIVER ADD CONSTRAINT FK_DRIVER_BUS 
    FOREIGN KEY (AssignedBusId) REFERENCES BUS(BusId);

-- Add health metrics and real-time location to BUS
ALTER TABLE BUS ADD CurrentLatitude FLOAT NULL DEFAULT 0.0;
ALTER TABLE BUS ADD CurrentLongitude FLOAT NULL DEFAULT 0.0;
ALTER TABLE BUS ADD TyreHealth FLOAT NULL DEFAULT 100.0;
ALTER TABLE BUS ADD EngineHealth FLOAT NULL DEFAULT 100.0;
ALTER TABLE BUS ADD ChassisHealth FLOAT NULL DEFAULT 100.0;

-- Add simulation fields to TRIP
ALTER TABLE TRIP ADD CurrentStopIndex INT NULL DEFAULT 0;
ALTER TABLE TRIP ADD SimulatedProgress FLOAT NULL DEFAULT 0.0;
ALTER TABLE TRIP ADD TripStatus VARCHAR(20) NULL DEFAULT 'IN_PROGRESS';

-- Add active/inactive flag to STOPS
ALTER TABLE STOPS ADD IsActive BIT NOT NULL DEFAULT 1;

-- Add estimated time per stop and route name to ROUTEE
ALTER TABLE ROUTEE ADD RouteName VARCHAR(150) NULL;
ALTER TABLE ROUTEE ADD EstimatedTimePerStop INT NOT NULL DEFAULT 120;

-- ============================================================
-- NEW TABLES — DO NOT EXIST YET, CREATE THEM
-- ============================================================

-- MAP table (stores map metadata; actual graph built from STOPS + ROUTEE)
CREATE TABLE MAP (
    MapId INT PRIMARY KEY IDENTITY(1,1),
    MapName VARCHAR(100) NOT NULL DEFAULT 'BOGO_MAIN_MAP',
    LastRefreshed DATETIME DEFAULT GETDATE(),
    TotalStops INT DEFAULT 0,
    TotalRoutes INT DEFAULT 0
);

-- Insert the singleton map record
INSERT INTO MAP (MapName) VALUES ('BOGO_MAIN_MAP');

-- PATHS table (computed paths for passenger bookings)
CREATE TABLE PATHS (
    PathId INT PRIMARY KEY IDENTITY(1,1),
    OriginStopId INT NOT NULL,
    DestinationStopId INT NOT NULL,
    TotalEstimatedTime INT NULL,
    CONSTRAINT FK_PATH_ORIGIN FOREIGN KEY (OriginStopId) REFERENCES STOPS(StopId),
    CONSTRAINT FK_PATH_DEST FOREIGN KEY (DestinationStopId) REFERENCES STOPS(StopId)
);

-- PATH_STOPS (ordered stops within a path)
CREATE TABLE PATH_STOPS (
    PathId INT NOT NULL,
    StopId INT NOT NULL,
    Position INT NOT NULL,
    RouteId INT NULL,  -- which route this leg of the path uses
    PRIMARY KEY (PathId, StopId),
    CONSTRAINT FK_PS_PATH FOREIGN KEY (PathId) REFERENCES PATHS(PathId),
    CONSTRAINT FK_PS_STOP FOREIGN KEY (StopId) REFERENCES STOPS(StopId),
    CONSTRAINT FK_PS_ROUTE FOREIGN KEY (RouteId) REFERENCES ROUTEE(RouteId)
);

-- Add PathId to BOOKING (link booking to a computed path)
ALTER TABLE BOOKING ADD PathId INT NULL;
ALTER TABLE BOOKING ADD CONSTRAINT FK_BOOKING_PATH 
    FOREIGN KEY (PathId) REFERENCES PATHS(PathId);

-- ============================================================
-- SEED DATA FOR SIMULATION (stops, routes, buses, trips)
-- Only run if no stops exist yet
-- ============================================================
-- Insert 5 stops forming a connected graph
-- (Rawalpindi/Islamabad imaginary coordinates for realism)
INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0479, 33.5651); -- Stop 1
INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0550, 33.5700); -- Stop 2
INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0620, 33.5750); -- Stop 3
INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0690, 33.5800); -- Stop 4
INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0760, 33.5850); -- Stop 5

-- Get location IDs (assumes these are IDs 1-5 if DB is fresh)
INSERT INTO STOPS (StopName, LocationId, Connections) VALUES ('Faizabad',     1, '2');
INSERT INTO STOPS (StopName, LocationId, Connections) VALUES ('Chandni Chowk',2, '1,3');
INSERT INTO STOPS (StopName, LocationId, Connections) VALUES ('Saddar',        3, '2,4');
INSERT INTO STOPS (StopName, LocationId, Connections) VALUES ('Liaquat Bagh',  4, '3,5');
INSERT INTO STOPS (StopName, LocationId, Connections) VALUES ('Pir Wadhai',    5, '4');

-- 2 Routes
INSERT INTO ROUTEE (Active, Stop_IDs, RouteName, EstimatedTimePerStop) 
    VALUES (1, '1,2,3', 'Route A - Faizabad to Saddar', 120);
INSERT INTO ROUTEE (Active, Stop_IDs, RouteName, EstimatedTimePerStop) 
    VALUES (1, '3,4,5', 'Route B - Saddar to Pir Wadhai', 120);

-- 2 Buses
INSERT INTO BUS (BusCompany, Registration, BusStatus, Capacity, TyreHealth, EngineHealth, ChassisHealth)
    VALUES ('BOGO Transit', 'LHR-001', 'ACTIVE', 50, 100.0, 100.0, 100.0);
INSERT INTO BUS (BusCompany, Registration, BusStatus, Capacity, TyreHealth, EngineHealth, ChassisHealth)
    VALUES ('BOGO Transit', 'LHR-002', 'ACTIVE', 50, 100.0, 100.0, 100.0);

-- Assign route+bus to the existing seed driver (UserId = 3, adjust if different)
UPDATE DRIVER SET AssignedRouteId = 1, AssignedBusId = 1 WHERE UserId = 3;

-- 2 Active Trips for simulation
INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime, CurrentStopIndex, SimulatedProgress, TripStatus)
    VALUES (1, 1, 3, GETDATE(), DATEADD(HOUR, 1, GETDATE()), 0, 0.0, 'IN_PROGRESS');
```

### 6.5 Handling Serialized Stop IDs in Java

Since `STOPS.Connections` and `ROUTEE.Stop_IDs` are stored as comma-separated strings, use these helpers in your repository layer:

```java
// Serialize List<Integer> to "1,2,3"
private String serializeIds(List<Integer> ids) {
    return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
}

// Deserialize "1,2,3" to List<Integer>
private List<Integer> deserializeIds(String serialized) {
    if (serialized == null || serialized.isBlank()) return new ArrayList<>();
    return Arrays.stream(serialized.split(","))
                 .map(String::trim)
                 .filter(s -> !s.isEmpty())
                 .map(Integer::parseInt)
                 .collect(Collectors.toList());
}
```

When loading a `Stop` from the DB:
1. Read `Connections` column as a String
2. Call `deserializeIds(connectionsString)` → get list of connected StopIds
3. For each connected StopId, fetch the `Stop` object (or lazy-load on demand)
4. Set `stop.setConnections(connectedStops)`

When loading a `Route` from `ROUTEE`:
1. Read `Stop_IDs` column as a String
2. Call `deserializeIds(stopIdsString)` → get ordered list of StopIds
3. Fetch each Stop in order and build `route.setStops(orderedStops)`

### 6.6 Authentication Query Pattern

Since password is in `PERSONAL_DETAILS` and role is determined by subtype tables:

```sql
-- Login query
SELECT u.UserId, pd.Name, pd.Email, pd.Password, pd.CNIC,
       CASE 
           WHEN a.UserId IS NOT NULL THEN 'ADMIN'
           WHEN d.UserId IS NOT NULL THEN 'DRIVER'
           WHEN p.UserId IS NOT NULL THEN 'PASSENGER'
       END AS Role
FROM USERS u
JOIN PERSONAL_DETAILS pd ON u.PdId = pd.PdId
LEFT JOIN ADMIN a ON u.UserId = a.UserId
LEFT JOIN DRIVER d ON u.UserId = d.UserId
LEFT JOIN PASSENGER p ON u.UserId = p.UserId
WHERE pd.Email = ? AND pd.Password = ?
```

Use `Email` as the login identifier (not username — there is no username column). The `Name` column in `PERSONAL_DETAILS` is the display name.

---

## 7. Building the Map System

### 7.1 MapRepository
`MapRepository.buildMap()` must:
1. Call `StopRepository.findAll()` — get all stops
2. For each stop, call `StopRepository.findConnections(stopId)` — populate `stop.connections`
3. Call `RouteRepository.findAll()` — get all routes
4. For each route, load its ordered stops from `RouteStops` table
5. Construct and return the `Map` singleton with all stops and routes populated

### 7.2 Four Map Views — Build in This Order

**Step 1: General Map** (baseline — all actors see this)
- Render all stops as nodes on a JavaFX canvas or `WebView` with a simple coordinate projection
- Render all route edges as lines connecting stops
- Overlay animated bus icons (see Section 10 — Simulation)
- Bus icons must move smoothly between stop positions

**Step 2: Booking Map** (passenger — already partially exists)
- Build on top of General Map
- Stops must be clickable — clicking a stop sets it as origin or destination
- After selection, call `BookingController.buildPath()` and highlight the computed path

**Step 3: Driver Route Map** (driver view — does not exist)
- Build on top of General Map
- Filter to show ONLY the driver's assigned route
- Each stop on the route must show a tooltip with a "Mark as Blocked" button
- On clicking "Mark as Blocked": call `MapController.blockStop(stopId, driverId)` → triggers rerouting

**Step 4: Admin Marking Map** (admin view — does not exist)
- Build on top of General Map
- Every stop and route is clickable
- Clicking a stop or route shows: "Mark Inactive" button
- On "Mark Route Inactive": call `MapController.markRouteInactive(routeId)` — deactivates route, no visual change required except greying out
- On "Mark Stop Inactive": call `MapController.markStopInactive(stopId)` — triggers rerouting algorithm on all routes containing that stop

---

## 8. Building the Path-Finding Algorithm

### 8.1 Data Structure
Represent the stop graph as `Map<Integer, List<Integer>>` (stopId → list of connected stopIds) for algorithm purposes.

### 8.2 Single-Leg Path (same route for origin and destination)
1. Find all routes containing originStop.
2. Find all routes containing destinationStop.
3. If intersection exists → single leg. Extract the sub-sequence of stops from origin to destination on that route.

### 8.3 Multi-Leg Path (transfer required)
1. BFS/Dijkstra from origin across the full stop graph.
2. When the destination is reached, record the full stop sequence.
3. Post-process: identify where the passenger crosses from one route to another → these are transfer points.
4. Build `Path.transferInstructions` list: e.g., `"Board Route A at Stop 1, travel to Stop 3, transfer to Route B, travel to Stop 6."`

### 8.4 Weight
Use `Route.estimatedTimePerStop` as the weight for edges. Each edge (stop-to-stop connection) has a weight equal to the `estimatedTimePerStop` of the route that connects them. If a connection is shared by multiple routes, use the minimum.

---

## 9. Building the New UI Pages

### 9.1 Add Stops / Routes Page

**File to create**: `AddStopsRoutes.fxml`

**Nav button to add**: In `AdminShell.fxml`'s navigation side panel, add a new `Button` with label `"Add Stops / Routes"` linking to this page. Style it identically to existing nav buttons using `NeonTheme.css` classes.

**Layout**: Two `VBox` containers side by side inside an `HBox`, styled as cards matching existing Admin resource boxes.

**Left Card — Add Stop**:
```
Label: "Add New Stop"
TextField: stopNameField (placeholder: "Stop Name")
TextField: latitudeField (placeholder: "Latitude")
TextField: longitudeField (placeholder: "Longitude")
Label: "Connected Stops:"
ListView<Stop>: connectionsListView (multi-select, populated from DB)
Button: "Add Stop" → calls ResourceController.addStop(...)
Label: statusLabel (shows success/error)
```

**Right Card — Add Route**:
```
Label: "Create New Route"
TextField: routeNameField (placeholder: "Route Name")
Label: "Select Stops (in order):"
ListView<Stop>: availableStopsListView
Button: "Add →" (moves selected stop to route list)
ListView<Stop>: routeStopsListView (ordered, shows selected stops in sequence)
Label: validationLabel (shows adjacency errors in red)
Button: "Create Route" → validates adjacency → calls ResourceController.addRoute(...)
Label: statusLabel
```

**Adjacency Validation** (client-side before server call):
- When user clicks "Create Route", iterate through `routeStopsListView`
- For each consecutive pair (stopA, stopB), check if stopB is in stopA's connections list
- If any pair is NOT adjacent, show error: `"Stop [B] is not adjacent to Stop [A]. Please select connected stops only."`
- Only call the backend if all pairs pass validation

### 9.2 Assign Route Page

**File to create**: `AssignRoute.fxml`

**Nav button to add**: In `AdminShell.fxml`, add `Button` with label `"Assign Route"` in the nav panel.

**Layout**:
```
Label: "Assign Route to Driver"
ComboBox<Driver>: driverComboBox (populated from DB — shows driver name)
ComboBox<Route>: routeComboBox (populated from DB — shows route name)
Button: "Assign" → calls ResourceController.assignRouteToDriver(driverId, routeId)
Label: statusLabel (shows "Route successfully assigned" or error)
TableView: currentAssignmentsTable
  Columns: Driver Name | Assigned Route | Assigned Bus
  (Populated on page load and refreshed after assignment)
```

### 9.3 System Analytics — Add Bus Box

The existing analytics page has two boxes: Drivers and Passengers. Add a third box for Buses:

```
Label: "Buses"
TextField: busSearchField (placeholder: "Search bus number...")
ListView<Bus>: busListView (scrollable, shows busNumber + status)
(Filter busListView on busSearchField text change)
```

Wire it: `AdminUIController` calls `ResourceController.getAllBuses()` on page load.

---

## 10. Bus Simulation Engine — Full Specification

### 10.1 Overview
The simulation makes the system appear live. Every active trip has a bus that moves between stops on its route. The bus pauses at stops and then resumes. This is visible on the General Map.

### 10.2 Implementation

Create a class `BusSimulationEngine.java` in `com.bogo.service` or `com.bogo.simulation`:

```java
// Pseudocode — implement fully in Java
public class BusSimulationEngine {
    private TripService tripService;
    private BusService busService;
    private ScheduledExecutorService scheduler;
    private static final int TICK_INTERVAL_MS = 500;     // update every 500ms
    private static final int DWELL_TIME_TICKS = 10;      // pause 5 seconds at stops
    private Map<Integer, Integer> dwellCounters = new HashMap<>(); // tripId → dwell ticks remaining

    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, 0, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        List<Trip> activeTrips = tripService.getAllActiveTrips();
        for (Trip trip : activeTrips) {
            Route route = trip.getRoute();
            List<Stop> stops = route.getStops();
            int idx = trip.getCurrentStopIndex();

            if (dwellCounters.getOrDefault(trip.getTripId(), 0) > 0) {
                // Bus is waiting at stop — count down dwell time
                dwellCounters.put(trip.getTripId(), dwellCounters.get(trip.getTripId()) - 1);
                continue;
            }

            if (idx >= stops.size() - 1) {
                // Last stop reached — loop back to start
                trip.setCurrentStopIndex(0);
                trip.setSimulatedProgress(0.0);
                tripService.updateTripProgress(trip.getTripId(), 0, 0.0);
                continue;
            }

            // Advance progress toward next stop
            double progressIncrement = 1.0 / (route.getEstimatedTimePerStop() / (TICK_INTERVAL_MS / 1000.0));
            double newProgress = trip.getSimulatedProgress() + progressIncrement;

            if (newProgress >= 1.0) {
                // Reached next stop
                int newIdx = idx + 1;
                trip.setCurrentStopIndex(newIdx);
                trip.setSimulatedProgress(0.0);
                dwellCounters.put(trip.getTripId(), DWELL_TIME_TICKS); // start dwell

                // Update bus location to exact stop coordinates
                Stop arrivedStop = stops.get(newIdx);
                busService.updateBusLocation(trip.getBus().getBusId(), arrivedStop.getLocation());
                tripService.updateTripProgress(trip.getTripId(), newIdx, 0.0);
            } else {
                // Interpolate bus location between current stop and next stop
                Stop fromStop = stops.get(idx);
                Stop toStop = stops.get(idx + 1);
                double lat = lerp(fromStop.getLocation().getLatitude(), toStop.getLocation().getLatitude(), newProgress);
                double lon = lerp(fromStop.getLocation().getLongitude(), toStop.getLocation().getLongitude(), newProgress);
                Location interpolated = new Location(lat, lon);
                busService.updateBusLocation(trip.getBus().getBusId(), interpolated);
                trip.setSimulatedProgress(newProgress);
                tripService.updateTripProgress(trip.getTripId(), idx, newProgress);
            }
        }

        // Notify UI to refresh bus positions (use JavaFX Platform.runLater)
        Platform.runLater(() -> notifyMapRefresh());
    }

    private double lerp(double a, double b, double t) { return a + (b - a) * t; }

    public void stop() { scheduler.shutdown(); }
}
```

### 10.3 Starting the Simulation
- `BusSimulationEngine.start()` must be called when the application launches — in `AdminApp.java`, `DriverApp.java`, and `PassengerApp.java` (all three apps should show buses moving on the general map).
- The General Map must subscribe to position updates and redraw bus icons on each tick via `Platform.runLater()`.
- Use imaginary/fixed travel times (e.g., 120 seconds per stop, 5 second dwell) — these values should be configurable constants.

---

## 11. Stop Rerouting Algorithm — Full Implementation

When a stop is marked blocked/inactive (by Admin OR Driver), call this on the affected route(s):

```java
// In ManageStopsService or ViewMapService
public Route rerouteAroundBlockedStop(Route route, Stop blockedStop) {
    List<Stop> stops = new ArrayList<>(route.getStops());
    int blockedIndex = stops.indexOf(blockedStop);
    if (blockedIndex <= 0 || blockedIndex >= stops.size() - 1) {
        // Blocked stop is first or last — cannot reroute gracefully
        // Log a warning; mark route inactive
        return route;
    }

    Stop prevStop = stops.get(blockedIndex - 1);
    Stop nextStop = stops.get(blockedIndex + 1);

    // Strategy 1: Find a single stop adjacent to both prev and next
    for (Stop candidate : prevStop.getConnections()) {
        if (candidate.getStopId() != blockedStop.getStopId()
                && nextStop.getConnections().contains(candidate)
                && candidate.isActive()) {
            stops.set(blockedIndex, candidate);
            route.setStops(stops);
            routeRepository.update(route);
            return route;
        }
    }

    // Strategy 2: BFS from prevStop to nextStop avoiding blockedStop
    List<Stop> bridgePath = bfsPath(prevStop, nextStop, blockedStop);
    if (bridgePath != null && bridgePath.size() > 2) {
        // Remove blockedStop, insert bridge stops
        stops.remove(blockedIndex);
        // Insert bridge (excluding prevStop and nextStop themselves)
        List<Stop> bridge = bridgePath.subList(1, bridgePath.size() - 1);
        stops.addAll(blockedIndex, bridge);
        route.setStops(stops);
        routeRepository.update(route);
    }
    return route;
}

private List<Stop> bfsPath(Stop from, Stop to, Stop avoid) {
    Queue<List<Stop>> queue = new LinkedList<>();
    queue.add(Arrays.asList(from));
    Set<Integer> visited = new HashSet<>();
    visited.add(from.getStopId());
    visited.add(avoid.getStopId());

    while (!queue.isEmpty()) {
        List<Stop> path = queue.poll();
        Stop current = path.get(path.size() - 1);
        for (Stop neighbor : current.getConnections()) {
            if (neighbor.getStopId() == to.getStopId()) {
                List<Stop> result = new ArrayList<>(path);
                result.add(to);
                return result;
            }
            if (!visited.contains(neighbor.getStopId()) && neighbor.isActive()) {
                visited.add(neighbor.getStopId());
                List<Stop> newPath = new ArrayList<>(path);
                newPath.add(neighbor);
                queue.add(newPath);
            }
        }
    }
    return null; // No path found
}
```

After rerouting, call `TripService` to update any active trips on the affected route so the simulation follows the new stop sequence.

---

## 12. Corrected Flow Reference

**⚠️ IMPORTANT CORRECTION** — The original technical document contained one error. The flow below is CORRECTED:

### Add Stop (Corrected)
```
AdminUIController
  → ResourceController.addStop(name, location, connectedStopIds)
    → ManageStopsService.addStop(...)         ← USE ManageStopsService (NOT RouteReviseService)
      → StopRepository.save(stop)
      → StopRepository.addConnection(...)     [for each connected stop]
```
`RouteReviseService` handles **routes only**. `ManageStopsService` handles **stops** (and their relation to routes for rerouting purposes).

---

## 13. Error Handling Standards

Every method that calls the database or performs complex logic must follow this pattern:

```java
public SomeResult someMethod(params) {
    try {
        // business logic + DB calls
        return result;
    } catch (SQLException e) {
        System.err.println("[BOGO ERROR] " + e.getMessage());
        throw new RuntimeException("Database error in someMethod: " + e.getMessage(), e);
    } catch (IllegalArgumentException e) {
        System.err.println("[BOGO VALIDATION] " + e.getMessage());
        throw e;
    }
}
```

In the UI layer (JavaFX controllers), wrap all controller calls in try-catch and show the user a dialog on failure:
```java
try {
    controller.doSomething();
    statusLabel.setText("Success");
    statusLabel.setStyle("-fx-text-fill: #00ff99;");
} catch (Exception e) {
    statusLabel.setText("Error: " + e.getMessage());
    statusLabel.setStyle("-fx-text-fill: #ff4444;");
}
```

---

## 14. Three JAR Build Configuration

### Maven Configuration (pom.xml)

Create three Maven profiles:

```xml
<profiles>
  <profile>
    <id>admin</id>
    <properties>
      <main.class>com.bogo.ui.admin.AdminApp</main.class>
      <jar.name>bogo-admin</jar.name>
    </properties>
  </profile>
  <profile>
    <id>driver</id>
    <properties>
      <main.class>com.bogo.ui.driver.DriverApp</main.class>
      <jar.name>bogo-driver</jar.name>
    </properties>
  </profile>
  <profile>
    <id>passenger</id>
    <properties>
      <main.class>com.bogo.ui.passenger.PassengerApp</main.class>
      <jar.name>bogo-passenger</jar.name>
    </properties>
  </profile>
</profiles>
```

Build commands:
```bash
mvn clean package -P admin     → produces bogo-admin.jar
mvn clean package -P driver    → produces bogo-driver.jar
mvn clean package -P passenger → produces bogo-passenger.jar
```

All three JARs must be runnable with: `java -jar bogo-admin.jar` (etc.)

---

## 15. Final Checklist Before Delivery

Before handing over the system, verify every item:

### Functional Completeness
- [ ] Admin can log in
- [ ] Admin can add a driver
- [ ] Admin can add a bus
- [ ] Admin can add a stop (with connections)
- [ ] Admin can create a route (with adjacency validation)
- [ ] Admin can assign a route to a driver
- [ ] Admin can view system analytics (drivers, buses, passengers — all three lists)
- [ ] Admin can view unresolved alerts
- [ ] Admin can resolve an alert (driver receives notification)
- [ ] Admin can mark a stop inactive on the Admin map
- [ ] Admin can mark a route inactive on the Admin map
- [ ] Driver can log in
- [ ] Driver can view their assigned route on the Driver Route Map
- [ ] Driver can mark a stop as blocked (rerouting triggers)
- [ ] Driver can send an alert (high/low priority, bus/driver down)
- [ ] Driver can view bus metrics (tyre, engine, chassis health)
- [ ] Driver receives notification when admin resolves their alert
- [ ] Passenger can sign up
- [ ] Passenger can log in
- [ ] Passenger can book a single-stop ride
- [ ] Passenger can book a multi-leg ride
- [ ] Passenger can cancel a booking
- [ ] Passenger can see their active bookings

### Map Completeness
- [ ] General Map shows all stops and routes
- [ ] General Map shows buses moving in real time (simulation)
- [ ] Buses pause at stops and then resume
- [ ] Booking Map allows stop selection and shows computed path
- [ ] Driver Route Map shows only the driver's route
- [ ] Driver Route Map stop tooltips have "Mark Blocked" button
- [ ] Admin Marking Map allows marking stops/routes inactive
- [ ] Rerouting algorithm works when a stop is blocked

### Technical
- [ ] Three separate JAR files build and run successfully
- [ ] All DB tables created with correct schema including new bus health fields
- [ ] No hardcoded credentials — DB config is externalized
- [ ] Simulation engine starts on app launch and stops cleanly on app close
- [ ] No unhandled exceptions crash the application

---

## 16. Notes for Antigravity

- The project deadline is **24 hours**. Prioritize working functionality over perfection.
- Start with the DB schema and repository layer — nothing works without data.
- Build and test each service in isolation before wiring to controllers.
- The simulation can use imaginary/fixed times — you do NOT need real GPS or actual time calculations.
- If the map rendering is complex, use a simple JavaFX `Canvas` with coordinate projection. Do NOT spend time integrating an external map library unless it is trivial.
- The UI already has a NeonTheme. Match it precisely. Do not introduce new visual styles.
- When in doubt about a design decision not covered in this document, choose the simpler, more robust option.
- Every feature must actually WORK. A feature that is built but crashes is worse than a feature that is not built yet.
