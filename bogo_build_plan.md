# BOGO — Build Plan & Status Tracker

## Project: Smart Public Bus Transport System
**Stack:** Java 17+, JavaFX 21, FXML, SQL Server, Maven  
**Deliverable:** 3 executable JARs — `bogo-admin.jar`, `bogo-driver.jar`, `bogo-passenger.jar`

---

## What Already Exists (Confirmed by Code Review)

### Architecture / Config
- `DatabaseConfig.java` — SQL Server (DESKTOP-M0688UR\SQLEXPRESS, db=bogo, user=BOGO)
- `module-info.java` — module descriptor
- `BOGOApplication.java` — single JavaFX entry (loads `/Main.fxml`)
- `BOGOUIController.java` — monolithic controller handling all 3 UIs

### Domain Layer (all exist)
- `PersonalDetails`, `User`, `Admin`, `Driver`, `Passenger`
- `Bus`, `BusStatus`, `Location`, `Stop`, `Route`, `Map`, `Trip`, `HelpingFunctions`
- **MISSING:** `Path`, `Booking`, `Alert`, `Resolution`, `Notification`

### Repository Layer
- `UserRepository` — full CRUD, email lookup, role detection (working)
- `StopRepository` — findAll (with connection parsing), save, update, delete
- `RouteRepository` — findAll (parses Stop_IDs), findById, addStopToRoute
- `BusRepository` — findAll, save, updateStatus
- `TripRepository` — findById, findLiveTripsWithBuses
- `BookingRepository`, `AlertRepository`, `NotificationRepository` — exist (partial)
- **MISSING:** `MapRepository`, `PathRepository`

### Service Layer
- `AuthService` — authenticate, register, logout (working)
- `PathBuildingService` — BFS pathfinding (working)
- `ManageStopsService` — addStop, validateStop (partial)
- `HandleDriverService` — skeleton only, most methods empty
- `ViewMapService` — loadMap, getStopsOnMap (partial)
- `TripService` — skeleton only (empty)
- `IssueReportingService`, `IssueResolvingService`, `NotificationService` — exist
- `BookingService`, `CancellationService` — exist
- `RouteReviseService` — skeleton
- `BusServices.java` — stub (61 bytes)
- `UserSignUpService.java` — stub (66 bytes)

### Controller Layer
- `BookingController`, `MessageController`, `ResourceController`, `UserController` — exist (partial)
- **MISSING:** `MapController`

### UI Layer
- Single entry point: `BOGOApplication.java` -> `Main.fxml`
- UI stubs: `AdminUI.java`, `DriverUI.java`, `PassengerUI.java`
- All FXML pages exist (Admin, Driver, Passenger shells + sub-pages)
- `NeonTheme.css` — full dark neon theme

---

## What Must Be Built

### Priority 1 — Critical
- [ ] DB Schema extension (ALTER TABLE + new tables MAP, PATHS, PATH_STOPS + seed data)
- [ ] `TripService.java` — getAllActiveTrips, updateTripProgress, createTrip
- [ ] `MapRepository.java` — buildMap, buildMapForRoute, refreshMap
- [ ] `PathRepository.java` — save(path), findById
- [ ] `MapController.java` — getFullMap, getMapForDriver, markStopInactive, blockStop
- [ ] `BusSimulationEngine.java` — background ScheduledExecutor, live bus movement
- [ ] General Map rendering — live bus icons, stop nodes, route edges on Canvas
- [ ] Driver Route Map — filter to assigned route, stop tooltips with Mark Blocked
- [ ] Admin Marking Map — click stops/routes to mark inactive

### Priority 2 — High
- [ ] `AddStopsRoutes.fxml` — new admin page
- [ ] `AssignRoute.fxml` — new admin page
- [ ] Nav buttons in `AdminShell.fxml` — "Add Stops/Routes" + "Assign Route"
- [ ] Bus box in `Admin_Analytics.fxml` — third analytics card
- [ ] `HandleDriverService` — implement addDriver, assignDriverToRoute, getAllDrivers
- [ ] `RouteReviseService` — implement addRoute, validateStopAdjacency
- [ ] `BusService.java` — full implementation (rename from BusServices.java)
- [ ] `UserSignUpService.java` — passenger registration
- [ ] `ResourceController` and `UserController` — wire all service calls
- [ ] Stop rerouting algorithm in `ManageStopsService`
- [ ] Bus health fields in `BusRepository` (TyreHealth, EngineHealth, ChassisHealth)

### Priority 3 — Three JARs
- [ ] `pom.xml` — three Maven profiles (admin/driver/passenger)
- [ ] `AdminApp.java`, `DriverApp.java`, `PassengerApp.java`
- [ ] Update `module-info.java` for new packages

---

## Database Schema — Actual Tables (SQL Server)

```
PERSONAL_DETAILS (PdId, Name, Email, Password, CNIC)
USERS            (UserId, PdId)
ADMIN            (UserId)
PASSENGER        (UserId)
DRIVER           (UserId, DriverID)
LOCATIONN        (LocationId, Longitude, Latitude)        <- double N
STOPS            (StopId, StopName, LocationId, Connections)
ROUTEE           (RouteId, Active, Stop_IDs)              <- double E
BUS              (BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity)
TRIP             (TripId, RouteId, BusId, DriverId, DepartureTime, ArrivalTime)
BOOKING          (BookingID, PassengerID, BusID, Active, Cost, BookingTime)
ALERTS           (AlertId, SenderDriverId, AlertType, Priority, Message, Status, SentTime)
RESOLUTIONS      (ResolutionId, AlertId, AdminId, NewBusId, NewDriverId, NewRouteId, ResolutionNotes, ResolvedAt)
NOTIFICATIONS    (NotificationId, RecipientUserId, Content, IsRead, CreatedAt)
```

### ALTER TABLE needed (run once via schema_extension.sql)
```sql
ALTER TABLE DRIVER ADD AssignedRouteId INT NULL;
ALTER TABLE DRIVER ADD AssignedBusId INT NULL;
ALTER TABLE BUS ADD CurrentLatitude FLOAT NULL DEFAULT 0.0;
ALTER TABLE BUS ADD CurrentLongitude FLOAT NULL DEFAULT 0.0;
ALTER TABLE BUS ADD TyreHealth FLOAT NULL DEFAULT 100.0;
ALTER TABLE BUS ADD EngineHealth FLOAT NULL DEFAULT 100.0;
ALTER TABLE BUS ADD ChassisHealth FLOAT NULL DEFAULT 100.0;
ALTER TABLE TRIP ADD CurrentStopIndex INT NULL DEFAULT 0;
ALTER TABLE TRIP ADD SimulatedProgress FLOAT NULL DEFAULT 0.0;
ALTER TABLE TRIP ADD TripStatus VARCHAR(20) NULL DEFAULT 'IN_PROGRESS';
ALTER TABLE STOPS ADD IsActive BIT NOT NULL DEFAULT 1;
ALTER TABLE ROUTEE ADD RouteName VARCHAR(150) NULL;
ALTER TABLE ROUTEE ADD EstimatedTimePerStop INT NOT NULL DEFAULT 120;
ALTER TABLE BOOKING ADD PathId INT NULL;
```

### New tables to CREATE
- MAP (singleton metadata record)
- PATHS (computed paths for bookings)
- PATH_STOPS (ordered stops within a path)

---

## Key Package Decisions
- Keep package `org.BOGO` (do NOT migrate to `com.bogo`)
- New simulation package: `org.BOGO.simulation`
- FXML path convention: `/AdminUI/AdminShell.fxml`, `/DriverUI/DriverShell.fxml`, etc.

---

## Build Execution Order

1. Phase 1: DB — schema_extension.sql + seed data
2. Phase 2: Domain — Path, Booking, Alert, Notification, Resolution classes
3. Phase 3: Repository — MapRepository, PathRepository + complete existing repos
4. Phase 4: Services — TripService, BusSimulationEngine, complete empties
5. Phase 5: Controllers — MapController + complete ResourceController
6. Phase 6: UI Wire — replace seedPreviewData with real DB calls
7. Phase 7: New FXML — AddStopsRoutes.fxml, AssignRoute.fxml, update AdminShell.fxml
8. Phase 8: Map Rendering — General/Driver/Admin map variants on Canvas
9. Phase 9: Simulation — integrate BusSimulationEngine
10. Phase 10: Three JARs — pom.xml profiles + App entry points

---

## Seed Credentials (already in DB — do NOT re-insert)
| Role | Email | Password |
|------|-------|----------|
| Admin | bogo.admin@gmail.com | AdminPass123 |
| Passenger | john.bogo@gmail.com | SecurePass123 |
| Driver | driver.bogo@gmail.com | DriveSafe2024 |
