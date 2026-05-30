# BOGO — Bus Operations & Governance Organizer (v2.0)

**Project Overview**
- **BOGO**: A desktop JavaFX application for managing bus operations — passenger booking, driver dashboards, admin tools, alerts, and an interactive route/map visualization.

**Tech Stack**
- Java (JDK 25)
- JavaFX (21.0.5)
- Microsoft SQL Server

**Primary Libraries / Files**
- `pom.xml`: Maven configuration and plugins. See [pom.xml](pom.xml).
- `module-info.java`: Java module declarations and exported packages. See [src/main/java/module-info.java](src/main/java/module-info.java).
- Main entrypoint: `org.BOGO.BOGOApplication` — [src/main/java/org/BOGO/BOGOApplication.java](src/main/java/org/BOGO/BOGOApplication.java).
- Main UI controller: `org.BOGO.BOGOUIController` — [src/main/java/org/BOGO/BOGOUIController.java](src/main/java/org/BOGO/BOGOUIController.java).
- Primary FXML/Assets: [src/main/resources/Main.fxml](src/main/resources/Main.fxml), [src/main/resources/NeonTheme.css](src/main/resources/NeonTheme.css) and other views under [src/main/resources](src/main/resources).
- Database schema and seeds: [schema.sql](schema.sql), `seed_*.sql` files at project root.

**Architecture & Design**
- Modular Java application (`module-info.java`) organized into packages: `controller`, `service`, `repository`, `domain`, `config`, and `UI`.
- Layered pattern (UI → Controller → Service → Repository → DB): Controllers coordinate UI actions, Services implement business logic (e.g., `PathBuildingService`), Repositories handle DB interactions.
- JavaFX MVC-style UI: FXML views map to controller classes; styling via CSS. The application uses a single `Application` subclass (`BOGOApplication`) as a central coordinator / singleton for shared services and state.
- Map & Path model: Domain objects under `org.BOGO.domain.transport` model stops, connections and maps. `PathBuildingService` builds routes from persisted map data and provides in-memory structures used by the map renderer in the UI.

**Features & Implementation Details**
- Interactive map rendering: Canvas/Pane-based rendering of stops and connections using JavaFX shapes (`Circle`, `Line`) and animations (`StrokeTransition`, `Glow`). (See `BOGOUIController`.)
- Booking workflow: Passenger booking UI uses `PathBuildingService` to compute routes and book rides via `BookingController`.
- Multi-role support: Admin, Driver, Passenger flows implemented via separate FXML shells and controllers under `src/main/resources/AdminUI`, `DriverUI`, and `PassengerUI`.
- Authentication: Simple login/signup UI wired to `UserController` (backed by DB repository code in `repository` package).
- Database-first model: SQL schema files provide the table definitions for Users, Drivers, Passengers, Routes, Stops, Buses, Bookings, Notifications.

---
