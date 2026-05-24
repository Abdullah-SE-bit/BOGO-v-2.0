# BOGO — Bus Operations & Governance Organizer (v2.0)

**Project Overview**
- **BOGO**: A desktop JavaFX application for managing bus operations — passenger booking, driver dashboards, admin tools, alerts, and an interactive route/map visualization.

**Tech Stack**
- **Java (JDK 25)**: Application language; module system (`module-info.java`) and modern Java language features (pattern matching, enhanced generics usage).
- **JavaFX (21.0.5)**: UI toolkit — FXML views, controllers, CSS theming, scene graph rendering for the interactive map.
- **Maven**: Build system and dependency management (`pom.xml`). Uses `javafx-maven-plugin` and `exec-maven-plugin` for running the app.
- **Microsoft SQL Server**: Relational database; JDBC driver `mssql-jdbc` used by repository layer. Schema scripts live at the project root.

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

**Notable Features & Implementation Details**
- Interactive map rendering: Canvas/Pane-based rendering of stops and connections using JavaFX shapes (`Circle`, `Line`) and animations (`StrokeTransition`, `Glow`). (See `BOGOUIController`.)
- Booking workflow: Passenger booking UI uses `PathBuildingService` to compute routes and book rides via `BookingController`.
- Multi-role support: Admin, Driver, Passenger flows implemented via separate FXML shells and controllers under `src/main/resources/AdminUI`, `DriverUI`, and `PassengerUI`.
- Authentication: Simple login/signup UI wired to `UserController` (backed by DB repository code in `repository` package).
- Alerts & Notifications: Message/alert lists in UI with placeholders for backend integration (see TODO tags in controllers).
- Database-first model: SQL schema files provide the table definitions for Users, Drivers, Passengers, Routes, Stops, Buses, Bookings, Notifications.

**Build & Run (Development)**
Prerequisites: JDK 25, Maven, and a running Microsoft SQL Server instance for full functionality.

Build and run with Maven (JavaFX plugin):

```bash
mvn clean package
mvn javafx:run
```

Or run using the exec plugin:

```bash
mvn -Dexec.mainClass=org.BOGO.BOGOApplication exec:java
```

If you hit module-related runtime errors, ensure your JAVA_HOME points to a JDK 25+ distribution and that Maven picks up the same Java.

**Database Setup**
- Create a database named `BOGO` in SQL Server and run the schema script: [schema.sql](schema.sql).
- The project uses the Microsoft JDBC driver configured in `pom.xml` — connection details are expected under the project's configuration (search `config` package or properties files).
- Seed data files (`seed_*.sql`) are included to populate demo users, stops and bookings for local testing.

**Key Files & Locations**
- Maven POM: [pom.xml](pom.xml)
- Java module declarations: [src/main/java/module-info.java](src/main/java/module-info.java)
- App entrypoint: [src/main/java/org/BOGO/BOGOApplication.java](src/main/java/org/BOGO/BOGOApplication.java)
- Main controller/UI: [src/main/java/org/BOGO/BOGOUIController.java](src/main/java/org/BOGO/BOGOUIController.java)
- Primary FXML: [src/main/resources/Main.fxml](src/main/resources/Main.fxml)
- Schema & seeds: [schema.sql](schema.sql), [seed_full.sql](seed_full.sql)

**Development Notes & TODOs (from codebase)**
- Several controllers include TODO comments indicating work left to integrate with repository/backend logic (search `TODO` in `src/main/java`).
- The app currently includes mock/preview data for dashboards and map demo generation; replace these with DB-backed repositories for production flows.
- Consider adding a configuration file (e.g., `application.properties`) for DB URL/credentials and enabling environment-specific profiles.

**Next Steps / How I can help**
- I can run a deeper scan to extract specific implementation links (repository classes and DB connection code).  
- I can wire a `.env` or config file and add setup instructions for local DB credentials.  
- I can run the app (if you want) and help fix runtime module/plugin issues.

---
