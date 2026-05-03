# BOGO — Smart Public Bus Transport System
## Complete Technical Context Document

---

## 1. Technology Stack

| Layer | Technology |
|---|---|
| **Backend Language** | Core Java (Java 17+ recommended) |
| **Frontend Framework** | JavaFX with FXML |
| **Styling** | CSS — file named `NeonTheme.css` |
| **Database** | Microsoft SQL Server |
| **Build Tool** | Maven (recommended) or Gradle |
| **Architecture Patterns** | Repository Pattern + MVC (Model-View-Controller) |
| **Design Patterns Used** | GRASP (General Responsibility Assignment Software Patterns), Gang of Four (GoF) patterns |
| **Deployment** | Three separate executable JAR files — one per actor (Admin, Driver, Passenger) |

---

## 2. Project Package Structure

The recommended package structure follows clean layered architecture:

```
com.bogo
├── config
│   └── DatabaseConfig.java
├── domain
│   ├── PersonalDetails.java
│   ├── User.java
│   ├── Admin.java
│   ├── Passenger.java
│   ├── Driver.java
│   ├── Bus.java
│   ├── Location.java
│   ├── Stop.java
│   ├── Route.java
│   ├── Map.java
│   ├── Path.java
│   ├── Booking.java
│   ├── Alert.java
│   ├── Resolution.java
│   ├── Notification.java
│   └── Trip.java
├── repository
│   ├── AlertRepository.java
│   ├── BookingRepository.java
│   ├── BusRepository.java
│   ├── NotificationRepository.java
│   ├── PathRepository.java           ← NEEDS TO BE CREATED
│   ├── RouteRepository.java
│   ├── StopRepository.java
│   ├── TripRepository.java
│   ├── UserRepository.java
│   └── MapRepository.java            ← NEEDS TO BE CREATED
├── service
│   ├── AuthService.java
│   ├── BookingService.java
│   ├── BusService.java
│   ├── CancellationService.java
│   ├── HandleDriverService.java
│   ├── IssueReportingService.java
│   ├── IssueResolvingService.java
│   ├── ManageStopsService.java
│   ├── NotificationService.java
│   ├── PathBuildingService.java
│   ├── RouteReviseService.java
│   ├── TripService.java
│   ├── UserSignUpService.java
│   └── ViewMapService.java
├── controller
│   ├── BookingController.java
│   ├── MessageController.java
│   ├── ResourceController.java
│   ├── UserController.java
│   └── MapController.java            ← NEEDS TO BE CREATED
├── ui
│   ├── admin
│   │   ├── AdminApp.java             ← Main entry for Admin JAR
│   │   └── AdminUIController.java
│   ├── driver
│   │   ├── DriverApp.java            ← Main entry for Driver JAR
│   │   └── DriverUIController.java
│   └── passenger
│       ├── PassengerApp.java         ← Main entry for Passenger JAR
│       └── PassengerUIController.java
└── resources
    ├── ui
    │   ├── AdminShell.fxml
    │   ├── DriverShell.fxml
    │   ├── PassengerShell.fxml
    │   ├── AddStopsRoutes.fxml       ← NEW — NEEDS TO BE CREATED
    │   ├── AssignRoute.fxml          ← NEW — NEEDS TO BE CREATED
    │   └── NeonTheme.css
    └── schema.sql
```

---

## 3. Configuration Layer

### 3.1 `DatabaseConfig.java`
**Package**: `com.bogo.config`

This is the sole configuration class in the system. It is responsible for:
- Establishing and managing the connection to the Microsoft SQL Server database.
- Providing a shared connection/data source object that all repository classes use.
- Should implement the **Singleton pattern** so only one DB connection pool is maintained throughout the application lifecycle.
- Reads DB credentials (host, port, database name, username, password) from a configuration file or environment variables.
- Connection string format for SQL Server: `jdbc:sqlserver://<host>:<port>;databaseName=<dbname>;user=<user>;password=<pass>`

---

## 4. Domain Layer

The domain layer contains all Plain Old Java Objects (POJOs) and entity classes that represent the real-world concepts of the system.

### 4.1 `PersonalDetails.java`
**Package**: `com.bogo.domain`

A value object / embedded object containing personal information. Fields:
- `String firstName`
- `String lastName`
- `String email`
- `String phoneNumber`
- `String address` (optional)

### 4.2 `User.java`
**Package**: `com.bogo.domain`

The base class for all user types. Maps to the `USERS` table joined with `PERSONAL_DETAILS`.
Role is determined by which subtype table (`ADMIN`, `PASSENGER`, `DRIVER`) contains the UserId.
Login identifier is **email** (stored in `PERSONAL_DETAILS.Email`) — there is no username column.
Password is stored in `PERSONAL_DETAILS.Password` (plaintext in current schema — antigravity may hash).

Fields:
- `int userId` (maps to `USERS.UserId`)
- `String role` (derived: `"ADMIN"`, `"PASSENGER"`, `"DRIVER"`)
- `PersonalDetails personalDetails`

Methods:
- `getUserId()`
- `getRole()`
- `getPersonalDetails()`

### 4.3 `Admin.java`
**Package**: `com.bogo.domain`
**Extends**: `User.java`

Represents an administrator. Admins are added directly to the database (not through the app sign-up flow). No additional fields beyond `User`, but may have admin-specific methods or flags.

### 4.4 `Passenger.java`
**Package**: `com.bogo.domain`
**Extends**: `User.java`

Represents a passenger. Passengers can self-register through the app.

Additional Fields:
- `List<Booking> activeBookings` (transient/in-memory, loaded on demand)

### 4.5 `Driver.java`
**Package**: `com.bogo.domain`
**Extends**: `User.java`

Represents a bus driver. Drivers are added only by Admins.

Additional Fields:
- `int driverId`
- `Route assignedRoute` (nullable — may not have a route assigned yet)
- `Bus assignedBus` (nullable)

### 4.6 `Bus.java`
**Package**: `com.bogo.domain`

Represents a physical bus in the fleet.

Fields:
- `int busId` (primary key, auto-generated)
- `String busNumber` (license plate or fleet number)
- `int capacity` (passenger capacity)
- `String status` (e.g., `"ACTIVE"`, `"DOWN"`, `"MAINTENANCE"`)
- `double tyreHealth` (percentage 0–100) ← **FIELD NEEDS TO BE ADDED TO DB**
- `double engineHealth` (percentage 0–100) ← **FIELD NEEDS TO BE ADDED TO DB**
- `double chassisHealth` (percentage 0–100) ← **FIELD NEEDS TO BE ADDED TO DB**
- `Location currentLocation` (for real-time simulation tracking)

### 4.7 `Location.java`
**Package**: `com.bogo.domain`

A value object representing a geographic coordinate.

Fields:
- `double latitude`
- `double longitude`
- `String label` (optional human-readable description)

### 4.8 `Stop.java`
**Package**: `com.bogo.domain`

Represents a physical bus stop. This is a **node in the transport graph**.

Fields:
- `int stopId` (primary key, auto-generated)
- `String stopName`
- `Location location`
- `List<Stop> connections` — **List of directly adjacent stops** (stops that a bus can reach directly from this stop without passing through another stop). This is the **adjacency list** of the graph.
- `boolean isActive` (default `true`; set to `false` by Admin or Driver when blocked)

Methods:
- `getConnections()` — returns the adjacency list
- `addConnection(Stop stop)`
- `removeConnection(Stop stop)`
- `isActive()`
- `setActive(boolean active)`

### 4.9 `Route.java`
**Package**: `com.bogo.domain`

Represents a fixed bus route — an ordered sequence of stops that a bus follows.

Fields:
- `int routeId` (primary key, auto-generated)
- `String routeName`
- `List<Stop> stops` — **ordered list** of stops on this route
- `boolean isActive` (default `true`; can be deactivated by Admin)
- `int estimatedTimePerStop` (simulated time in seconds between consecutive stops, used for bus movement simulation)

Methods:
- `getStops()`
- `addStop(Stop stop)`
- `removeStop(Stop stop)`
- `getNextStop(Stop currentStop)` — returns the next stop after the given stop in the route
- `getPreviousStop(Stop currentStop)`

### 4.10 `Map.java`
**Package**: `com.bogo.domain`

The singleton representation of the entire transport network. This is a **graph** containing all stops and routes.

Fields:
- `List<Route> routes`
- `List<Stop> stops`
- `static Map instance` (Singleton instance)

Methods:
- `static Map getInstance()`
- `addRoute(Route route)`
- `addStop(Stop stop)`
- `getRoutes()`
- `getStops()`
- `Stop findStopById(int stopId)`
- `Route findRouteById(int routeId)`
- `List<Stop> getAdjacentStops(Stop stop)` — returns `stop.getConnections()`
- `List<Stop> findAlternativePath(Stop from, Stop to)` — graph traversal (BFS/Dijkstra) to reroute around blocked stops

### 4.11 `Path.java`
**Package**: `com.bogo.domain`

Represents a passenger's planned journey. A path can span **multiple routes** (multi-leg travel).

Fields:
- `int pathId`
- `List<Stop> stops` — the ordered sequence of stops the passenger will travel through
- `List<Route> routes` — the routes used in this path (a path may use more than one route)
- `Stop origin`
- `Stop destination`
- `int totalEstimatedTime` (sum of travel times across all legs)
- `List<String> transferInstructions` — human-readable instructions like "Transfer to Route 3 at Stop X"

Methods:
- `getStops()`
- `getRoutes()`
- `isMultiLeg()` — returns `true` if `routes.size() > 1`

### 4.12 `Booking.java`
**Package**: `com.bogo.domain`

Represents a booking made by a passenger.

Fields:
- `int bookingId` (primary key, auto-generated)
- `Passenger passenger`
- `Path path`
- `LocalDateTime bookingTime`
- `String status` (`"ACTIVE"`, `"CANCELLED"`, `"COMPLETED"`)

### 4.13 `Alert.java`
**Package**: `com.bogo.domain`

Represents an alert/issue sent by a driver to the Admin.

Fields:
- `int alertId` (primary key, auto-generated)
- `Driver driver` (the sender)
- `Bus bus` (the bus involved)
- `String alertType` (`"BUS_DOWN"` or `"DRIVER_DOWN"`)
- `String priority` (`"HIGH"` or `"LOW"`)
- `String message`
- `LocalDateTime timestamp`
- `boolean isResolved` (default `false`)

### 4.14 `Resolution.java`
**Package**: `com.bogo.domain`

Represents the Admin's response/resolution to an alert.

Fields:
- `int resolutionId`
- `Alert alert` (the alert being resolved)
- `Admin resolvedBy`
- `String resolutionMessage`
- `LocalDateTime resolvedAt`

### 4.15 `Notification.java`
**Package**: `com.bogo.domain`

Represents a notification delivered to a user.

Fields:
- `int notificationId`
- `User recipient`
- `String message`
- `LocalDateTime timestamp`
- `boolean isRead` (default `false`)
- `String type` (`"ALERT_RESOLUTION"`, `"ROUTE_ASSIGNMENT"`, etc.)

### 4.16 `Trip.java`
**Package**: `com.bogo.domain`

Represents an active trip — the complex relationship between a **Driver**, a **Bus**, and a **Route** at a given time. This is the operational entity that drives the simulation.

Fields:
- `int tripId` (primary key, auto-generated)
- `Bus bus`
- `Route route`
- `Driver driver`
- `LocalDateTime startTime`
- `LocalDateTime estimatedEndTime`
- `Stop currentStop` (the stop the bus is currently at or heading towards)
- `int currentStopIndex` (index in `route.getStops()`)
- `String tripStatus` (`"IN_PROGRESS"`, `"COMPLETED"`, `"CANCELLED"`)
- `double simulatedProgress` (0.0 to 1.0, representing how far between two stops the bus currently is — used for map animation)

---

## 5. Repository Layer

Repository classes handle all database operations (CRUD). Each repository takes a `DatabaseConfig` or `Connection` object and executes SQL queries.

Every repository should implement a corresponding interface for testability:
- e.g., `interface IUserRepository` implemented by `UserRepository`

### 5.1 `UserRepository.java`
**Package**: `com.bogo.repository`

Handles DB operations for all `User` subtypes (Admin, Driver, Passenger).

Methods:
- `User findById(int userId)`
- `User findByUsername(String username)`
- `List<User> findAll()`
- `List<Driver> findAllDrivers()`
- `List<Passenger> findAllPassengers()`
- `List<Admin> findAllAdmins()`
- `void save(User user)` — INSERT
- `void update(User user)` — UPDATE
- `void delete(int userId)`
- `boolean existsByUsername(String username)`
- `User authenticate(String username, String passwordHash)` — used by AuthService

### 5.2 `BusRepository.java`
**Package**: `com.bogo.repository`

Methods:
- `Bus findById(int busId)`
- `List<Bus> findAll()`
- `void save(Bus bus)`
- `void update(Bus bus)` — used to update status, health metrics, location
- `void delete(int busId)`
- `void updateStatus(int busId, String status)`
- `void updateHealthMetrics(int busId, double tyreHealth, double engineHealth, double chassisHealth)`
- `void updateLocation(int busId, double latitude, double longitude)` — for simulation

### 5.3 `RouteRepository.java`
**Package**: `com.bogo.repository`

Methods:
- `Route findById(int routeId)`
- `List<Route> findAll()`
- `List<Route> findActiveRoutes()`
- `void save(Route route)`
- `void update(Route route)`
- `void delete(int routeId)`
- `void setActive(int routeId, boolean active)`
- `void addStopToRoute(int routeId, int stopId, int position)` — ordered insert

### 5.4 `StopRepository.java`
**Package**: `com.bogo.repository`

Methods:
- `Stop findById(int stopId)`
- `List<Stop> findAll()`
- `List<Stop> findByName(String name)` — search functionality
- `void save(Stop stop)`
- `void update(Stop stop)`
- `void delete(int stopId)`
- `void setActive(int stopId, boolean active)`
- `void addConnection(int stopId, int connectedStopId)` — bidirectional adjacency
- `void removeConnection(int stopId, int connectedStopId)`
- `List<Stop> findConnections(int stopId)` — adjacency list lookup

### 5.5 `BookingRepository.java`
**Package**: `com.bogo.repository`

Methods:
- `Booking findById(int bookingId)`
- `List<Booking> findByPassengerId(int passengerId)`
- `List<Booking> findActiveByPassengerId(int passengerId)`
- `void save(Booking booking)`
- `void cancel(int bookingId)` — sets status to `"CANCELLED"`
- `void delete(int bookingId)`

### 5.6 `AlertRepository.java`
**Package**: `com.bogo.repository`

Methods:
- `Alert findById(int alertId)`
- `List<Alert> findAll()`
- `List<Alert> findUnresolved()`
- `void save(Alert alert)`
- `void markResolved(int alertId)`
- `void delete(int alertId)`

### 5.7 `NotificationRepository.java`
**Package**: `com.bogo.repository`

Methods:
- `List<Notification> findByRecipientId(int userId)`
- `List<Notification> findUnreadByRecipientId(int userId)`
- `void save(Notification notification)`
- `void markRead(int notificationId)`

### 5.8 `TripRepository.java`
**Package**: `com.bogo.repository`

Methods:
- `Trip findById(int tripId)`
- `List<Trip> findAll()`
- `List<Trip> findActive()` — trips with status `"IN_PROGRESS"`
- `Trip findByDriverId(int driverId)`
- `Trip findByBusId(int busId)`
- `void save(Trip trip)`
- `void update(Trip trip)` — update currentStop, progress, status
- `void delete(int tripId)`

### 5.9 `MapRepository.java` ← **TO BE CREATED**
**Package**: `com.bogo.repository`

Note: The map is not stored as a single object in the DB. Instead, this repository constructs the `Map` domain object by aggregating data from `StopRepository` and `RouteRepository`.

Methods:
- `Map buildMap()` — fetches all stops (with their connections) and all routes, constructs and returns the `Map` singleton
- `Map buildMapForRoute(int routeId)` — builds a partial map containing only stops relevant to a specific route
- `void refreshMap(Map map)` — re-fetches data and updates the singleton

### 5.10 `PathRepository.java` ← **TO BE CREATED**
**Package**: `com.bogo.repository`

Handles persistence of computed paths for bookings.

Methods:
- `Path findById(int pathId)`
- `void save(Path path)` — persists the path and its associated stops/routes
- `void delete(int pathId)`

---

## 6. Service Layer

Service classes contain all business logic. They are injected with repository objects and coordinate between repositories and domain objects.

### 6.1 `AuthService.java`
**Package**: `com.bogo.service`
**Dependencies**: `UserRepository`

Handles authentication (login) for all user types.

Methods:
- `User login(String username, String password)` — hashes the password, calls `UserRepository.authenticate()`, returns the `User` object (or throws `AuthException`)
- `void logout(User user)` — clears session
- `User getCurrentUser()` — returns the currently logged-in user
- `boolean isAuthenticated()`

### 6.2 `UserSignUpService.java`
**Package**: `com.bogo.service`
**Dependencies**: `UserRepository`

Handles passenger self-registration.

Methods:
- `void registerPassenger(String username, String password, PersonalDetails details)` — validates input, hashes password, calls `UserRepository.save()`
- `boolean isUsernameTaken(String username)`
- `void validatePassword(String password)` — enforces password rules

### 6.3 `HandleDriverService.java`
**Package**: `com.bogo.service`
**Dependencies**: `UserRepository`, `BusRepository`, `RouteRepository`

Handles Admin adding/managing drivers.

Methods:
- `void addDriver(String username, String password, PersonalDetails details)` — Admin-only action; creates Driver and saves via `UserRepository`
- `void assignRouteToDriver(int driverId, int routeId)` — assigns a route to a driver; updates DB; triggers notification to driver
- `void assignBusToDriver(int driverId, int busId)`
- `Driver getDriverById(int driverId)`
- `List<Driver> getAllDrivers()`
- `void removeDriver(int driverId)`

### 6.4 `BusService.java`
**Package**: `com.bogo.service`
**Dependencies**: `BusRepository`

Handles all bus-related operations.

Methods:
- `void addBus(Bus bus)` — saves a new bus to the DB
- `Bus getBusById(int busId)`
- `List<Bus> getAllBuses()`
- `void updateBusStatus(int busId, String status)`
- `void updateBusMetrics(int busId, double tyreHealth, double engineHealth, double chassisHealth)`
- `void updateBusLocation(int busId, Location location)` — called by simulation engine
- `void removeBus(int busId)`

### 6.5 `RouteReviseService.java`
**Package**: `com.bogo.service`
**Dependencies**: `RouteRepository`

Handles adding and modifying routes.

Methods:
- `void addRoute(String routeName, List<Integer> stopIds)` — validates that stops are adjacent, creates Route, saves via `RouteRepository`
- `void deactivateRoute(int routeId)`
- `void activateRoute(int routeId)`
- `Route getRouteById(int routeId)`
- `List<Route> getAllRoutes()`
- `boolean validateStopAdjacency(List<Stop> stops)` — verifies that each stop in the list is connected to the next one (adjacency validation)

### 6.6 `ManageStopsService.java`
**Package**: `com.bogo.service`
**Dependencies**: `StopRepository`, `RouteRepository`

Handles adding and modifying stops.

**NOTE**: In the technical PDF, the "Add Stop" flow incorrectly routes through `RouteReviseService`. The correct service for stop creation is `ManageStopsService` because it has access to `StopRepository`. This document corrects that discrepancy.

Methods:
- `void addStop(String name, Location location, List<Integer> connectedStopIds)` — creates a Stop, sets connections, saves via `StopRepository`
- `void deactivateStop(int stopId)` — marks stop inactive; triggers rerouting
- `void activateStop(int stopId)`
- `void addConnectionBetweenStops(int stopId1, int stopId2)`
- `List<Stop> getAllStops()`
- `Stop getStopById(int stopId)`
- `List<Stop> searchStops(String query)` — for search bar functionality
- `List<Stop> getAlternativeStopsForReroute(Stop blockedStop, Route affectedRoute)` — finds adjacent stop to reroute around a blocked stop using graph traversal

### 6.7 `BookingService.java`
**Package**: `com.bogo.service`
**Dependencies**: `BookingRepository`, `BusRepository`, `UserRepository`

Handles creating and managing bookings.

Methods:
- `Booking createBooking(int passengerId, Path path)` — creates a Booking with status `"ACTIVE"`, saves to DB
- `List<Booking> getPassengerBookings(int passengerId)`
- `Booking getBookingById(int bookingId)`

### 6.8 `CancellationService.java`
**Package**: `com.bogo.service`
**Dependencies**: `BookingRepository`

Handles booking cancellations.

Methods:
- `void cancelBooking(int bookingId)` — sets booking status to `"CANCELLED"` in DB
- `boolean canCancel(int bookingId)` — checks if booking is still `"ACTIVE"`

### 6.9 `PathBuildingService.java`
**Package**: `com.bogo.service`
**Dependencies**: `MapRepository` (or `StopRepository` + `RouteRepository` to reconstruct the map)

The most algorithmically complex service. Responsible for computing the optimal path for a passenger.

Algorithm Used: **Dijkstra's Shortest Path** or **BFS** on the stop graph, with route-awareness for multi-leg journeys.

Methods:
- `Path buildPath(int originStopId, int destinationStopId)` — single-leg or multi-leg path
- `Path buildMultiStopPath(List<Integer> stopIds)` — builds a path through multiple user-specified stops
- `List<Path> findAlternativePaths(int originStopId, int destinationStopId)` — returns top N paths by travel time
- `List<Route> getRoutesContainingStop(Stop stop)` — used internally for multi-leg detection
- `Stop findTransferStop(Route route1, Route route2)` — finds the stop where a passenger can transfer between two routes
- `boolean isStopOnRoute(Stop stop, Route route)`
- `int calculateTravelTime(Path path)` — sums up estimated times across all legs

**Multi-leg logic**:
1. Find all routes that contain the origin stop.
2. Find all routes that contain the destination stop.
3. If there is a common route — single leg journey.
4. If no common route — find a transfer stop (a stop that appears on both a route from origin and a route to destination).
5. Build the full path including the transfer instruction.
6. Return the path with minimum total travel time.

### 6.10 `IssueReportingService.java`
**Package**: `com.bogo.service`
**Dependencies**: `BusRepository`, `AlertRepository`

Handles drivers sending alerts.

Methods:
- `void sendAlert(int driverId, int busId, String alertType, String priority, String message)` — creates an `Alert`, saves to `AlertRepository`; if alertType is `"BUS_DOWN"`, also updates bus status to `"DOWN"` via `BusRepository`

### 6.11 `IssueResolvingService.java`
**Package**: `com.bogo.service`
**Dependencies**: `BusRepository`, `TripRepository`, `AlertRepository`, `NotificationRepository`

Handles Admin resolving alerts.

Methods:
- `List<Alert> getAllUnresolvedAlerts()`
- `void resolveAlert(int alertId, String resolutionMessage, int adminId)` — marks alert as resolved in `AlertRepository`; creates a `Resolution`; creates a `Notification` for the driver and saves via `NotificationRepository`; if bus was marked down, optionally update bus status

### 6.12 `NotificationService.java`
**Package**: `com.bogo.service`
**Dependencies**: `NotificationRepository`

Methods:
- `List<Notification> getUnreadNotifications(int userId)`
- `List<Notification> getAllNotifications(int userId)`
- `void markAsRead(int notificationId)`
- `void sendNotification(int recipientId, String message, String type)` — creates and saves a Notification

### 6.13 `ViewMapService.java`
**Package**: `com.bogo.service`
**Dependencies**: `RouteRepository`, `StopRepository`

Builds map data for UI rendering.

Methods:
- `Map getFullMap()` — returns complete Map (all routes + stops with adjacency)
- `Map getMapForRoute(int routeId)` — returns map filtered to one route
- `Map getMapForDriver(int driverId)` — gets the assigned route for a driver and returns map for it
- `List<Bus> getActiveBusLocations()` — for general map real-time overlay
- `void markStopInactive(int stopId)` — Admin action; calls ManageStopsService logic
- `void markRouteInactive(int routeId)` — Admin action
- `Route rerouteAroundBlockedStop(Route route, Stop blockedStop)` — triggers graph search to find alternative

### 6.14 `TripService.java`
**Package**: `com.bogo.service`
**Dependencies**: `TripRepository`

Handles trip lifecycle and simulation updates.

Methods:
- `Trip createTrip(int driverId, int busId, int routeId)` — creates and starts a Trip
- `void updateTripProgress(int tripId, int currentStopIndex, double progress)` — called by simulation engine periodically
- `Trip getTripByDriverId(int driverId)`
- `List<Trip> getAllActiveTrips()` — used by general map to show all buses
- `void completeTrip(int tripId)`
- `void cancelTrip(int tripId)`

---

## 7. Controller Layer

Controllers receive calls from the UI layer (JavaFX UI Controllers) and delegate to the appropriate services. Controllers do NOT contain business logic — they are pure coordinators.

### 7.1 `BookingController.java`
**Package**: `com.bogo.controller`
**Contains Services**:
- `BookingService`
- `PathBuildingService`
- `CancellationService`

Methods:
- `Path buildPath(int originStopId, int destinationStopId)` → delegates to `PathBuildingService`
- `Path buildMultiStopPath(List<Integer> stopIds)` → delegates to `PathBuildingService`
- `Booking confirmBooking(int passengerId, Path path)` → delegates to `BookingService`
- `List<Booking> getPassengerBookings(int passengerId)` → delegates to `BookingService`
- `void cancelBooking(int bookingId)` → delegates to `CancellationService`

### 7.2 `MessageController.java`
**Package**: `com.bogo.controller`
**Contains Services**:
- `IssueReportingService`
- `IssueResolvingService`
- `NotificationService`

Methods:
- `void sendAlert(int driverId, int busId, String alertType, String priority, String message)` → delegates to `IssueReportingService`
- `List<Alert> getUnresolvedAlerts()` → delegates to `IssueResolvingService`
- `void resolveAlert(int alertId, String resolutionMessage, int adminId)` → delegates to `IssueResolvingService`
- `List<Notification> getNotifications(int userId)` → delegates to `NotificationService`
- `void markNotificationRead(int notificationId)` → delegates to `NotificationService`

### 7.3 `ResourceController.java`
**Package**: `com.bogo.controller`
**Contains Services**:
- `HandleDriverService`
- `RouteReviseService`
- `ManageStopsService`
- `BusService`
- `TripService`

Methods:
- `void addDriver(String username, String password, PersonalDetails details)` → `HandleDriverService`
- `void addBus(Bus bus)` → `BusService`
- `void addStop(String name, Location location, List<Integer> connectedStopIds)` → `ManageStopsService`
- `void addRoute(String routeName, List<Integer> stopIds)` → `RouteReviseService`
- `void assignRouteToDriver(int driverId, int routeId)` → `HandleDriverService`
- `List<Driver> getAllDrivers()` → `HandleDriverService`
- `List<Bus> getAllBuses()` → `BusService`
- `List<Route> getAllRoutes()` → `RouteReviseService`
- `Trip createTrip(int driverId, int busId, int routeId)` → `TripService`
- `List<Trip> getAllActiveTrips()` → `TripService`

### 7.4 `UserController.java`
**Package**: `com.bogo.controller`
**Contains Services**:
- `AuthService`
- `UserSignUpService`

Methods:
- `User login(String username, String password)` → `AuthService`
- `void logout()` → `AuthService`
- `void registerPassenger(String username, String password, PersonalDetails details)` → `UserSignUpService`
- `User getCurrentUser()` → `AuthService`

### 7.5 `MapController.java` ← **TO BE CREATED — DOES NOT EXIST YET**
**Package**: `com.bogo.controller`
**Contains Services**:
- `ViewMapService`

Methods:
- `Map getFullMap()` → `ViewMapService`
- `Map getMapForDriver(int driverId)` → `ViewMapService`
- `List<Bus> getActiveBusLocations()` → `ViewMapService` (for real-time overlay)
- `void markStopInactive(int stopId)` → `ViewMapService` (Admin action)
- `void markRouteInactive(int routeId)` → `ViewMapService` (Admin action)
- `void blockStop(int stopId, int driverId)` → `ViewMapService` (Driver action)

---

## 8. UI Layer — JavaFX FXML Files

### 8.1 Existing FXML Files (DO NOT DELETE — MAY MODIFY WITH ADDITIONS)

- **`AdminShell.fxml`**: The main container for the Admin UI. Has a side navigation panel with buttons for each Admin feature. New navigation buttons must be added here for:
  - "Add Stops / Routes" (new page)
  - "Assign Route" (new page)
- **`DriverShell.fxml`**: Main container for Driver UI. Contains navigation for: Send Alert, View Route (map), Bus Metrics.
- **`PassengerShell.fxml`**: Main container for Passenger UI. Contains: Book Ride, Cancel Booking, View Map.

### 8.2 New FXML Files to Be Created

#### `AddStopsRoutes.fxml`
Linked from Admin navigation. Contains two panels side by side:

**Panel 1 — Add Stop**:
- Text field: Stop Name
- Text fields: Latitude, Longitude (or integrated map picker)
- Multi-select list/checkboxes: "Connected Stops" (shows all existing stops, admin selects which ones are adjacent)
- Button: "Add Stop"

**Panel 2 — Add Route**:
- Multi-select ordered list: "Select Stops" (admin picks stops in order)
- Validation: Each selected stop must be adjacent (connected) to the next selected stop — show error if not
- Text field: Route Name
- Button: "Create Route"

Styling: Add to `NeonTheme.css` — match existing Admin panel neon aesthetic.

#### `AssignRoute.fxml`
Linked from Admin navigation ("Assign Route" button in `AdminShell.fxml` nav panel). Contains:
- Dropdown/ComboBox: Select Driver (lists all drivers by name)
- Dropdown/ComboBox: Select Route (lists all active routes)
- Button: "Assign Route"
- Confirmation display showing current assignments

---

## 9. CSS Styling

**File**: `NeonTheme.css`  
**Location**: `src/main/resources/ui/NeonTheme.css`

All new UI pages must follow this CSS file. New rules must be **appended** to `NeonTheme.css` — do not create separate CSS files. Apply the same neon color scheme, font styles, and component styles present in the existing file.

---

## 10. Complete User Flow Specifications

### 10.1 Admin Flows

#### Add Driver
```
AdminShell.fxml (Add Resource button)
  → AdminUIController
    → ResourceController.addDriver(username, password, personalDetails)
      → HandleDriverService.addDriver(...)
        → UserRepository.save(driver)
          → SQL INSERT into Users table
```

#### Add Bus
```
AdminShell.fxml (Add Resource button)
  → AdminUIController
    → ResourceController.addBus(bus)
      → BusService.addBus(bus)
        → BusRepository.save(bus)
          → SQL INSERT into Buses table
```

#### Add Stop *(CORRECTED — ManageStopsService, NOT RouteReviseService)*
```
AddStopsRoutes.fxml (Add Stop form)
  → AdminUIController
    → ResourceController.addStop(name, location, connectedStopIds)
      → ManageStopsService.addStop(name, location, connectedStopIds)
        → StopRepository.save(stop)
          → SQL INSERT into Stops table
        → StopRepository.addConnection(stopId, connectedStopId) [for each connection]
```

#### Add Route
```
AddStopsRoutes.fxml (Add Route form)
  → AdminUIController
    → ResourceController.addRoute(routeName, stopIds)
      → RouteReviseService.addRoute(routeName, stopIds)
        → RouteReviseService.validateStopAdjacency(stops) [throws if invalid]
        → RouteRepository.save(route)
          → SQL INSERT into Routes table
        → RouteRepository.addStopToRoute(routeId, stopId, position) [for each stop]
```

#### Assign Route to Driver *(NEW — DOES NOT EXIST YET)*
```
AssignRoute.fxml
  → AdminUIController
    → ResourceController.assignRouteToDriver(driverId, routeId)
      → HandleDriverService.assignRouteToDriver(driverId, routeId)
        → UserRepository.update(driver) [update assignedRoute field]
        → NotificationService.sendNotification(driverId, "You have been assigned Route X", "ROUTE_ASSIGNMENT")
          → NotificationRepository.save(notification)
```

#### View System Analytics
```
AdminShell.fxml (Analytics tab)
  → AdminUIController
    → ResourceController.getAllDrivers() → HandleDriverService → UserRepository [list of drivers]
    → ResourceController.getAllBuses() → BusService → BusRepository [list of buses]
    → UserController.getAllPassengers() [or via ResourceController] → AuthService → UserRepository [passengers]
    → UI renders: searchable drivers list, searchable passengers list, scrollable buses list (with search bar)
```

#### Resolve Issue
```
AdminShell.fxml (Resolve Issue tab)
  → AdminUIController
    → MessageController.getUnresolvedAlerts()
      → IssueResolvingService.getAllUnresolvedAlerts()
        → AlertRepository.findUnresolved()
    → [Admin clicks on alert, writes resolution]
    → MessageController.resolveAlert(alertId, resolutionMessage, adminId)
      → IssueResolvingService.resolveAlert(...)
        → AlertRepository.markResolved(alertId)
        → NotificationRepository.save(notification for driver)
```

### 10.2 Driver Flows

#### Send Alert
```
DriverShell.fxml (Send Alert button)
  → DriverUIController
    → MessageController.sendAlert(driverId, busId, alertType, priority, message)
      → IssueReportingService.sendAlert(...)
        → AlertRepository.save(alert)
        → BusRepository.updateStatus(busId, "DOWN") [if alertType == "BUS_DOWN"]
```

#### View Route on Map *(MAP VARIANT — TO BE BUILT)*
```
DriverShell.fxml (View Route tab)
  → DriverUIController
    → MapController.getMapForDriver(driverId)
      → ViewMapService.getMapForDriver(driverId)
        → HandleDriverService.getDriverById(driverId) [get assigned route]
        → MapRepository.buildMapForRoute(routeId) [builds map for that route]
    → UI renders map with ONLY driver's route highlighted
    → Each stop on route has a tooltip with "Mark as Blocked" button
    → On "Mark as Blocked": MapController.blockStop(stopId, driverId)
      → ViewMapService reroutes via graph traversal
```

#### View Bus Metrics
```
DriverShell.fxml (Bus Metrics button)
  → DriverUIController
    → ResourceController.getBusById(busId) [busId from driver's assigned bus]
      → BusService.getBusById(busId)
        → BusRepository.findById(busId)
    → UI renders: Tyre Health, Engine Health, Chassis Health as gauges/progress bars
```

### 10.3 Passenger Flows

#### Book Ride (Single or Multi-Stop)
```
PassengerShell.fxml (Book Ride)
  → PassengerUIController
    → User searches and selects origin stop and destination stop(s) on map
    → BookingController.buildPath(originStopId, destinationStopId)
      → PathBuildingService.buildPath(...)
        → MapRepository.buildMap() [or use cached Map singleton]
        → [Run Dijkstra/BFS on stop graph]
        → Returns Path object with stops, routes, transfer instructions
    → UI shows passenger the path with instructions
    → User confirms booking
    → BookingController.confirmBooking(passengerId, path)
      → BookingService.createBooking(passengerId, path)
        → PathRepository.save(path)
        → BookingRepository.save(booking)
```

#### Cancel Booking
```
PassengerShell.fxml (Main screen booking list)
  → User clicks a booking → "Cancel" button appears
  → PassengerUIController
    → BookingController.cancelBooking(bookingId)
      → CancellationService.cancelBooking(bookingId)
        → BookingRepository.cancel(bookingId) [sets status to CANCELLED]
```

---

## 11. Map System — Technical Specification

### 11.1 Graph Model

The stop network is modeled as an **undirected weighted graph**:
- **Nodes**: `Stop` objects
- **Edges**: Connections between stops (stored in `Stop.connections` as adjacency list)
- **Weights**: `estimatedTimePerStop` from the `Route` object (or a flat time value if no route-specific timing)

### 11.2 Map Types and Their Rendering Requirements

| Map Type | Data Source | Special Rendering |
|---|---|---|
| General Map | All routes + stops + active trips | Animated bus icons moving along routes |
| Booking Map | All stops (for stop selection) | Clickable stops for origin/destination selection |
| Admin Marking Map | All routes + stops | Clickable stops/routes for deactivation |
| Driver Route Map | Single assigned route's stops | Tooltip per stop with "Mark Blocked" button |

### 11.3 Bus Simulation Engine

The simulation engine runs on a background thread (JavaFX `Timeline` or `ScheduledExecutorService`) and:

1. Fetches all active `Trip` objects from `TripService.getAllActiveTrips()`
2. For each trip, calculates the bus's current position between two stops based on elapsed time and `Route.estimatedTimePerStop`
3. Updates `Trip.simulatedProgress` and `Trip.currentStopIndex`
4. Calls `BusService.updateBusLocation(busId, location)` — interpolates the bus's `Location` between the two stop coordinates based on progress (0.0 to 1.0)
5. When progress reaches 1.0, advances to the next stop, pauses for a simulated dwell time (e.g., 5 seconds), then resets progress and advances `currentStopIndex`
6. When the last stop is reached, the trip is marked complete and optionally restarts (loop)
7. The JavaFX map UI is updated via `Platform.runLater()` to keep it on the UI thread

### 11.4 Stop Rerouting Algorithm

When a stop is marked blocked/inactive:

```
Input: affectedRoute (Route), blockedStop (Stop)
Output: modified Route with alternative stop(s) replacing blockedStop

1. prevStop = route.getPreviousStop(blockedStop)
2. nextStop = route.getNextStop(blockedStop)
3. alternativeStop = null

4. For each stop S in prevStop.getConnections():
     if S != blockedStop AND S is in nextStop.getConnections():
         alternativeStop = S
         break

5. If alternativeStop found:
     replace blockedStop with alternativeStop in route.stops

6. Else (no direct alternative):
     Run BFS from prevStop to nextStop avoiding blockedStop
     Get shortest intermediate path [prevStop, s1, s2, ..., nextStop]
     Insert s1, s2, ... into route.stops in place of blockedStop

7. Save updated route via RouteRepository.update(route)
8. Notify all affected active trips via TripService
```

---

## 12. Database Schema Notes

The following tables must exist (full schema to be provided separately as `schema.sql`). **New fields to be added by Antigravity** are marked with ← NEW.

### Users Table
```sql
Users (
  userId INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) UNIQUE NOT NULL,
  passwordHash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,  -- 'ADMIN', 'DRIVER', 'PASSENGER'
  firstName VARCHAR(100),
  lastName VARCHAR(100),
  email VARCHAR(150),
  phoneNumber VARCHAR(20),
  assignedRouteId INT FOREIGN KEY → Routes(routeId),  -- for DRIVER
  assignedBusId INT FOREIGN KEY → Buses(busId)         -- for DRIVER
)
```

### Buses Table
```sql
Buses (
  busId INT PRIMARY KEY AUTO_INCREMENT,
  busNumber VARCHAR(50) UNIQUE NOT NULL,
  capacity INT,
  status VARCHAR(20),         -- 'ACTIVE', 'DOWN', 'MAINTENANCE'
  currentLatitude DOUBLE,
  currentLongitude DOUBLE,
  tyreHealth DOUBLE,          ← NEW FIELD
  engineHealth DOUBLE,        ← NEW FIELD
  chassisHealth DOUBLE        ← NEW FIELD
)
```

### Stops Table
```sql
Stops (
  stopId INT PRIMARY KEY AUTO_INCREMENT,
  stopName VARCHAR(150) NOT NULL,
  latitude DOUBLE,
  longitude DOUBLE,
  isActive BIT DEFAULT 1
)
```

### StopConnections Table (Adjacency)
```sql
StopConnections (
  stopId INT FOREIGN KEY → Stops(stopId),
  connectedStopId INT FOREIGN KEY → Stops(stopId),
  PRIMARY KEY (stopId, connectedStopId)
)
```

### Routes Table
```sql
Routes (
  routeId INT PRIMARY KEY AUTO_INCREMENT,
  routeName VARCHAR(150) NOT NULL,
  isActive BIT DEFAULT 1,
  estimatedTimePerStop INT DEFAULT 120  -- seconds
)
```

### RouteStops Table (Ordered)
```sql
RouteStops (
  routeId INT FOREIGN KEY → Routes(routeId),
  stopId INT FOREIGN KEY → Stops(stopId),
  position INT,   -- order in route (1, 2, 3...)
  PRIMARY KEY (routeId, stopId)
)
```

### Paths Table
```sql
Paths (
  pathId INT PRIMARY KEY AUTO_INCREMENT,
  originStopId INT FOREIGN KEY → Stops(stopId),
  destinationStopId INT FOREIGN KEY → Stops(stopId),
  totalEstimatedTime INT
)
```

### PathStops Table
```sql
PathStops (
  pathId INT FOREIGN KEY → Paths(pathId),
  stopId INT FOREIGN KEY → Stops(stopId),
  position INT,
  PRIMARY KEY (pathId, stopId)
)
```

### Bookings Table
```sql
Bookings (
  bookingId INT PRIMARY KEY AUTO_INCREMENT,
  passengerId INT FOREIGN KEY → Users(userId),
  pathId INT FOREIGN KEY → Paths(pathId),
  bookingTime DATETIME,
  status VARCHAR(20)  -- 'ACTIVE', 'CANCELLED', 'COMPLETED'
)
```

### Alerts Table
```sql
Alerts (
  alertId INT PRIMARY KEY AUTO_INCREMENT,
  driverId INT FOREIGN KEY → Users(userId),
  busId INT FOREIGN KEY → Buses(busId),
  alertType VARCHAR(20),   -- 'BUS_DOWN', 'DRIVER_DOWN'
  priority VARCHAR(10),    -- 'HIGH', 'LOW'
  message TEXT,
  timestamp DATETIME,
  isResolved BIT DEFAULT 0
)
```

### Notifications Table
```sql
Notifications (
  notificationId INT PRIMARY KEY AUTO_INCREMENT,
  recipientId INT FOREIGN KEY → Users(userId),
  message TEXT,
  timestamp DATETIME,
  isRead BIT DEFAULT 0,
  type VARCHAR(50)
)
```

### Trips Table
```sql
Trips (
  tripId INT PRIMARY KEY AUTO_INCREMENT,
  driverId INT FOREIGN KEY → Users(userId),
  busId INT FOREIGN KEY → Buses(busId),
  routeId INT FOREIGN KEY → Routes(routeId),
  startTime DATETIME,
  estimatedEndTime DATETIME,
  currentStopIndex INT DEFAULT 0,
  simulatedProgress DOUBLE DEFAULT 0.0,
  tripStatus VARCHAR(20)  -- 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
)
```

---

## 13. Deployment — Three Separate JARs

The system must be packaged as **three separate executable JAR files**:

| JAR File | Entry Point Class | Actors Who Use It |
|---|---|---|
| `bogo-admin.jar` | `com.bogo.ui.admin.AdminApp` | System Administrators |
| `bogo-driver.jar` | `com.bogo.ui.driver.DriverApp` | Bus Drivers |
| `bogo-passenger.jar` | `com.bogo.ui.passenger.PassengerApp` | Passengers |

Each JAR:
- Contains all backend classes (domain, repository, service, controller) — these are shared
- Only the UI layer and the main entry point differ
- All three JARs connect to the **same database**
- Use Maven profiles or Gradle variants to produce three JARs from one project

---

## 14. ACTUAL DATABASE SCHEMA MAPPING (SQL Server — Confirmed from schema.sql)

> ⚠️ This section supersedes any schema described elsewhere in this document. The tables below are the REAL tables in the database.

### 14.1 Existing Tables and Their Exact Column Names

**PERSONAL_DETAILS**
```sql
PdId INT PRIMARY KEY IDENTITY(1,1)
Name VARCHAR(30) NOT NULL
Email VARCHAR(100) NULL         ← used as login identifier
Password VARCHAR(50) NULL       ← plaintext in current schema
CNIC VARCHAR(13) NULL
```

**USERS**
```sql
UserId INT PRIMARY KEY IDENTITY(1,1)
PdId INT NOT NULL → PERSONAL_DETAILS(PdId)
```

**ADMIN** (table-per-type, extends USERS)
```sql
UserId INT PRIMARY KEY → USERS(UserId)
```

**PASSENGER** (table-per-type, extends USERS)
```sql
UserId INT PRIMARY KEY → USERS(UserId)
```

**DRIVER** (table-per-type, extends USERS)
```sql
UserId INT PRIMARY KEY → USERS(UserId)
DriverID CHAR(13) NOT NULL
-- TO BE ADDED via ALTER TABLE:
AssignedRouteId INT NULL → ROUTEE(RouteId)
AssignedBusId INT NULL → BUS(BusId)
```

**LOCATIONN** (double N — exact table name)
```sql
LocationId INT PRIMARY KEY IDENTITY(1,1)
Longitude FLOAT NOT NULL
Latitude FLOAT NOT NULL
```

**STOPS**
```sql
StopId INT PRIMARY KEY IDENTITY(1,1)
StopName VARCHAR(50) NOT NULL
LocationId INT NOT NULL → LOCATIONN(LocationId)
Connections NVARCHAR(MAX) NOT NULL   ← serialized comma-separated string of adjacent StopIds e.g. "2,5,7"
-- TO BE ADDED via ALTER TABLE:
IsActive BIT NOT NULL DEFAULT 1
```

**ROUTEE** (double E — exact table name)
```sql
RouteId INT PRIMARY KEY IDENTITY(1,1)
Active BIT NOT NULL
Stop_IDs NVARCHAR(MAX) NOT NULL      ← serialized ordered comma-separated StopIds e.g. "1,3,5,7"
-- TO BE ADDED via ALTER TABLE:
RouteName VARCHAR(150) NULL
EstimatedTimePerStop INT DEFAULT 120
```

**BUS**
```sql
BusId INT PRIMARY KEY IDENTITY(1,1)
BusCompany VARCHAR(50) NOT NULL
Registration VARCHAR(7) NULL
RegistrationYear INT NULL
BusStatus VARCHAR(20) NULL
Capacity INT NULL
-- TO BE ADDED via ALTER TABLE:
CurrentLatitude FLOAT NULL DEFAULT 0.0
CurrentLongitude FLOAT NULL DEFAULT 0.0
TyreHealth FLOAT NULL DEFAULT 100.0
EngineHealth FLOAT NULL DEFAULT 100.0
ChassisHealth FLOAT NULL DEFAULT 100.0
```

**TRIP**
```sql
TripId INT PRIMARY KEY IDENTITY(1,1)
RouteId INT NOT NULL → ROUTEE(RouteId)
BusId INT NOT NULL → BUS(BusId)
DriverId INT NOT NULL → DRIVER(UserId)
DepartureTime DATETIME NOT NULL
ArrivalTime DATETIME NOT NULL
-- TO BE ADDED via ALTER TABLE:
CurrentStopIndex INT DEFAULT 0
SimulatedProgress FLOAT DEFAULT 0.0
TripStatus VARCHAR(20) DEFAULT 'IN_PROGRESS'
```

**BOOKING**
```sql
BookingID INT PRIMARY KEY IDENTITY(1,1)
PassengerID INT NOT NULL → PASSENGER(UserId)
BusID INT NOT NULL → BUS(BusId)
Active BIT NOT NULL
Cost FLOAT NOT NULL
BookingTime DATETIME NOT NULL
-- TO BE ADDED via ALTER TABLE:
PathId INT NULL → PATHS(PathId)
```

**ALERTS**
```sql
AlertId INT PRIMARY KEY IDENTITY(1,1)
SenderDriverId INT NOT NULL → DRIVER(UserId)
AlertType VARCHAR(20) NOT NULL        -- 'BUS_DOWN', 'DRIVER_DOWN', 'ROUTE_BLOCKAGE'
Priority VARCHAR(10) NOT NULL         -- 'HIGH', 'LOW'
Message NVARCHAR(MAX)
Status VARCHAR(20) DEFAULT 'PENDING' -- 'PENDING', 'RESOLVED'
SentTime DATETIME DEFAULT GETDATE()
```

**RESOLUTIONS**
```sql
ResolutionId INT PRIMARY KEY IDENTITY(1,1)
AlertId INT NOT NULL → ALERTS(AlertId)
AdminId INT NOT NULL → ADMIN(UserId)
NewBusId INT NULL → BUS(BusId)
NewDriverId INT NULL → DRIVER(UserId)
NewRouteId INT NULL → ROUTEE(RouteId)
ResolutionNotes NVARCHAR(MAX)
ResolvedAt DATETIME DEFAULT GETDATE()
```

**NOTIFICATIONS**
```sql
NotificationId INT PRIMARY KEY IDENTITY(1,1)
RecipientUserId INT NOT NULL → USERS(UserId)
Content NVARCHAR(MAX) NOT NULL
IsRead BIT DEFAULT 0
CreatedAt DATETIME DEFAULT GETDATE()
```

### 14.2 New Tables to Create (Do Not Exist Yet)

**MAP** (singleton record — graph built dynamically from STOPS + ROUTEE)
```sql
MapId INT PRIMARY KEY IDENTITY(1,1)
MapName VARCHAR(100) NOT NULL DEFAULT 'BOGO_MAIN_MAP'
LastRefreshed DATETIME DEFAULT GETDATE()
TotalStops INT DEFAULT 0
TotalRoutes INT DEFAULT 0
```

**PATHS**
```sql
PathId INT PRIMARY KEY IDENTITY(1,1)
OriginStopId INT NOT NULL → STOPS(StopId)
DestinationStopId INT NOT NULL → STOPS(StopId)
TotalEstimatedTime INT NULL
```

**PATH_STOPS**
```sql
PathId INT NOT NULL → PATHS(PathId)
StopId INT NOT NULL → STOPS(StopId)
Position INT NOT NULL
RouteId INT NULL → ROUTEE(RouteId)
PRIMARY KEY (PathId, StopId)
```

### 14.3 Existing Seed Data (Already in DB — Do Not Re-insert)

| Role | Name | Email | Password | Notes |
|---|---|---|---|---|
| Admin | Super Admin | bogo.admin@gmail.com | AdminPass123 | UserId=1 (assumed) |
| Passenger | John Passenger | john.bogo@gmail.com | SecurePass123 | |
| Driver | Fast Driver | driver.bogo@gmail.com | DriveSafe2024 | DriverID: DRI-5566-2024 |

### 14.4 Domain Class → DB Table Mapping Correction

Due to the actual schema, domain class field names must map as follows:

| Domain Class | Field | DB Table | DB Column |
|---|---|---|---|
| `PersonalDetails` | `pdId` | `PERSONAL_DETAILS` | `PdId` |
| `PersonalDetails` | `name` | `PERSONAL_DETAILS` | `Name` |
| `PersonalDetails` | `email` | `PERSONAL_DETAILS` | `Email` |
| `PersonalDetails` | `password` | `PERSONAL_DETAILS` | `Password` |
| `PersonalDetails` | `cnic` | `PERSONAL_DETAILS` | `CNIC` |
| `User` | `userId` | `USERS` | `UserId` |
| `Driver` | `driverId` (license) | `DRIVER` | `DriverID` |
| `Driver` | `assignedRouteId` | `DRIVER` | `AssignedRouteId` (ALTER) |
| `Stop` | `connections` | `STOPS` | `Connections` (serialized string) |
| `Stop` | `locationId` | `LOCATIONN` | `LocationId` |
| `Route` | `stops` | `ROUTEE` | `Stop_IDs` (serialized string) |
| `Route` | `active` | `ROUTEE` | `Active` |
| `Bus` | `busCompany` | `BUS` | `BusCompany` |
| `Bus` | `registration` | `BUS` | `Registration` |
| `Bus` | `tyreHealth` | `BUS` | `TyreHealth` (ALTER) |
| `Alert` | `alertId` | `ALERTS` | `AlertId` |
| `Alert` | `driverId` | `ALERTS` | `SenderDriverId` |
| `Alert` | `status` | `ALERTS` | `Status` ('PENDING'/'RESOLVED') |
| `Notification` | `content` | `NOTIFICATIONS` | `Content` |
| `Notification` | `recipientId` | `NOTIFICATIONS` | `RecipientUserId` |
| `Resolution` | `resolutionId` | `RESOLUTIONS` | `ResolutionId` |
| `Booking` | `bookingId` | `BOOKING` | `BookingID` |
| `Booking` | `passengerId` | `BOOKING` | `PassengerID` |
| `Trip` | `tripId` | `TRIP` | `TripId` |
| `Trip` | `routeId` | `TRIP` | `RouteId` |
