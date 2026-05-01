# BOGO SYSTEM - MASTER AI IMPLEMENTATION GUIDE

**System Prompt for AI Coding Agent:**
You are an expert full-stack Java developer specializing in JavaFX, plain JDBC, and complex system architectures. You are tasked with generating the complete backend logic, database operations, and UI wiring for the "BOGO" Bus Management System. 

**Context:** The SQL Server Database is **already created and active**. Do NOT write DDL (CREATE TABLE) scripts. Your task is to write the Java Domain models, JDBC Repositories, Business Services, API-style Controllers, and wire them to the JavaFX UI Controllers through a central Mediator (`BOGOApplication`).

---

## 📁 1. Project Architecture & Paths
Ensure all generated files are strictly placed in these directories under the root package `org.BOGO`:
* `config/`: `DatabaseConfig.java`
* `domain/`: Data models (`User`, `Bus`, `Stop`, `Route`, `Trip`, `Alert`, etc.)
* `repository/`: JDBC CRUD classes (`UserRepository`, `BusRepository`, etc.)
* `service/`: Business logic (`AuthService`, `PathBuildingService`, `IssueResolvingService`, etc.)
* `controller/`: Backend controllers (`UserController`, `BookingController`, `ResourceController`, `MessageController`)
* `UI/`: JavaFX View Controllers (`AdminShellController`, `PassengerDashboardController`, etc.)
* `/`: `BOGOApplication.java` (The Singleton Mediator)
* `src/main/resources/`: FXML files and `NeonTheme.css`

---

## 🗄️ 2. Database Schema Reference (Active DB)
*Use these exact table and column names for your JDBC `PreparedStatement` queries:*

* **PERSONAL_DETAILS:** `PdId` (PK), `Name`, `Email`, `Password`, `CNIC`
* **USERS:** `UserId` (PK), `PdId` (FK)
* **ADMIN / PASSENGER:** `UserId` (PK/FK)
* **DRIVER:** `UserId` (PK/FK), `DriverID` (CHAR 13)
* **LOCATIONN:** `LocationId` (PK), `Longitude`, `Latitude`
* **STOPS:** `StopId` (PK), `StopName`, `LocationId` (FK), `Connections` (NVARCHAR(MAX) - comma separated IDs)
* **ROUTEE:** `RouteId` (PK), `Active` (BIT), `Stop_IDs` (NVARCHAR(MAX) - comma separated IDs)
* **BUS:** `BusId` (PK), `BusCompany`, `Registration`, `RegistrationYear`, `BusStatus`, `Capacity`
* **TRIP:** `TripId` (PK), `RouteId` (FK), `BusId` (FK), `DriverId` (FK), `DepartureTime`, `ArrivalTime`
* **BOOKING:** `BookingID` (PK), `PassengerID` (FK), `BusID` (FK), `Active` (BIT), `Cost` (FLOAT), `BookingTime`
* **ALERTS:** `AlertId` (PK), `SenderDriverId` (FK), `AlertType`, `Priority`, `Message`, `Status`, `SentTime`
* **RESOLUTIONS:** `ResolutionId` (PK), `AlertId` (FK), `AdminId` (FK), `NewBusId`, `NewDriverId`, `NewRouteId`, `ResolvedAt`
* **NOTIFICATIONS:** `NotificationId` (PK), `RecipientUserId` (FK), `Content`, `IsRead` (BIT), `CreatedAt`

---

## 🧱 3. Domain Model Specifications
* **User Hierarchy:** `User` (Abstract) contains `PersonalDetails` and `userID`. `Passenger`, `Admin`, and `Driver` extend `User`.
* **Map & Graph:** * `Map` contains `ArrayList<Stop>` and `ArrayList<Route>`.
    * `Stop` contains `ArrayList<Stop> connections` and `ArrayList<Integer> connectionRoutesId`. *Crucial: When parsing from DB, split the `Connections` NVARCHAR string.*
* **Trip:** Represents the active link between a Bus, Route, Driver, and current progress.

---

## 🛠️ 4. Repository Implementation Requirements (JDBC)
All repositories must use `DatabaseConfig.getConnection()`. Do not use ORMs.
1.  **`UserRepository`:** * Must handle Polymorphic inserts. For a new Passenger: `INSERT INTO PERSONAL_DETAILS` -> Get `PdId` -> `INSERT INTO USERS` -> Get `UserId` -> `INSERT INTO PASSENGER`.
    * Use `Statement.RETURN_GENERATED_KEYS`.
2.  **`StopRepository`:** * Parse the `Connections` string into Java Lists. When updating, serialize the Java Lists back into a comma-separated string.
3.  **`TripRepository`:** * Must be able to `SELECT` trips joined with buses to get live locations/status.

---

## ⚙️ 5. Core Services Logic

### A. Graph & Pathfinding (`PathBuildingService`)
* **Initialization:** On system start, query `StopRepository` and `RouteRepository` to fully populate the `Map` singleton.
* **Algorithm:** Implement Dijkstra's or BFS to find the shortest/cheapest path between `startStop` and `endStop`.

### B. Dynamic Resource Resolution (`IssueResolvingService` & `HandleDriverService`)
* **Context:** Admin clicks "Resolve" on a `BUS_DOWN` alert.
* **Logic:**
    1. Update `BUS` table: set old bus status to `BUS_DOWN`.
    2. Query `BusRepository` for a bus where `BusStatus = 'AVAILABLE'`.
    3. Update `TRIP` table: swap the old `BusId` with the new `BusId`.
    4. Insert record into `RESOLUTIONS` tracking the swap.
    5. Update `ALERTS` table: set status to `RESOLVED`.
    6. Insert into `NOTIFICATIONS` for the driver: "Your new bus is [ID]".

### C. Authentication (`AuthService`)
* Query `PERSONAL_DETAILS` joined with `USERS`. Check which sub-table (`ADMIN`, `PASSENGER`, `DRIVER`) the `UserId` exists in to instantiate the correct Domain object.

---

## 🔌 6. UI to Backend Wiring (The Mediator Pattern)
The `BOGOApplication` class holds the global state and instances of the Controllers.

**Example UI Wiring (Agent must follow this pattern for all UI actions):**
```java
// Inside Passenger_BookRideController (JavaFX)
@FXML private ComboBox<Stop> combo_stop_1;
@FXML private ComboBox<Stop> combo_stop_2;
@FXML private Button btn_confirm_booking;

@FXML
public void handleBookingAction() {
    Stop start = combo_stop_1.getValue();
    Stop end = combo_stop_2.getValue();
    Passenger currentUser = (Passenger) BOGOApplication.getInstance().getCurrentUser();
    
    // Pass to Backend Controller
    boolean success = BOGOApplication.getInstance().getBookingController().bookRide(currentUser, start, end);
    
    if(success) {
        // Show success Neon UI Toast
    }
}
```

---

## 🚦 7. Execution Plan for the AI Agent
To prevent dependency errors, implement the system in this exact order:
1.  **Domain Layer:** Generate all classes, enums (`BusStatus`), and lists.
2.  **Database Config:** Implement `DatabaseConfig.java`.
3.  **Repository Layer:** Implement CRUD operations using JDBC.
4.  **Service Layer:** Implement algorithms, transaction handling, and DB logic calling the repositories.
5.  **Controller Layer:** Implement the 5 main controllers (`User`, `Booking`, `Resource`, `Message`).
6.  **Mediator:** Implement `BOGOApplication.java` with static instance and `currentUser` tracking.
7.  **UI Controllers:** Connect the generated FXML IDs to the JavaFX controllers and route all button clicks to the `BOGOApplication` mediator.


with open("BOGO_Master_Implementation_Guide.md", "w") as f:
    f.write(master_markdown)

print("[file-tag: BOGO_Master_Implementation_Guide.md]")