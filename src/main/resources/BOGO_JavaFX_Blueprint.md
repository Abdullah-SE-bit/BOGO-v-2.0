# BOGO - Bus Management System (JavaFX AI Implementation Blueprint)

**System Prompt for AI Agent:**
You are an expert JavaFX and CSS developer. Your task is to read this comprehensive blueprint and generate all the required FXML, CSS, and Java files to build the "BOGO" UI. Follow the exact folder structures, layouts, FXIDs, CSS classes, and backend marker comments provided below. Do not deviate from the requested layout containers (VBox, HBox, BorderPane, etc.) or the validation rules. 

---

## 📁 1. Project Folder Structure
Please ensure the generated files are placed exactly in these directories:
* `src/main/resources/` (Main, MapView, Theme)
* `src/main/resources/AdminUI/`
* `src/main/resources/DriverUI/`
* `src/main/resources/PassengerUI/`
* `src/main/java/org/BOGO/` (Java controllers and main preview app)

---

## 🎨 2. The "NeonTheme" (CSS)
**File:** `src/main/resources/NeonTheme.css`
**Instructions:** Create a comprehensive CSS file using this color palette:
* **Background:** Deep Midnight Blue (`#0a0b1e`)
* **Primary Accent/Borders:** Neon Cyan/Blue (`#00f2ff`)
* **Action/Success:** Neon Green (`#39ff14`)
* **Alternative Action:** Electric Purple (`#bc13fe`)

**Required CSS Classes:**
* `.root`: Background `#0a0b1e`.
* `.header-text`: Font-weight bold, white text, glowing cyan text-fill/drop-shadow.
* `.neon-card`: Deep blue background, transparent slightly, `#00f2ff` border (radius 10px), subtle drop shadow.
* `.neon-text-field`: `#0a0b1e` background, border only on the bottom (`#00f2ff`), white text. Focus glow effect.
* `.button-green`: Background `#39ff14`, black text, bold, border-radius 5px.
* `.button-purple`: Background `#bc13fe`, white text, bold, border-radius 5px.
* `.nav-panel`: Background slightly darker than root, right border `#00f2ff`.
* `.nav-button`: Transparent background, white text.
* `.nav-button-active`: Transparent background, `#00f2ff` left-border (width 5px), bright white text.
* `.scroll-bar`: Track color `#0a0b1e`, Thumb color `#00f2ff` with 10px radius.
* `.gauge-indicator`: Specifically for circular progress indicators (green, yellow, red based on value).

---

## 🌐 3. Shared & Main Components

### 3.1 Main Page
**File:** `src/main/resources/Main.fxml`
* **Layout:** `StackPane` root (applies `.root` style). Center contains an `HBox` (spacing 50, center aligned).
* **Content:** 3 `VBox` elements acting as entity cards (Admin, Passenger, Driver).
* **Card Specs:** Apply `.neon-card`. Each has an `ImageView` (or FontIcon), a `Label` (entity name), and a `Button`.
* **IDs:** `btn_admin`, `btn_passenger`, `btn_driver`.
* **Routing Logic:** Clicks should route to their respective Login FXMLs.

### 3.2 Map View Placeholder
**File:** `src/main/resources/MapView_ALL.fxml`
* **Layout:** `StackPane` with an inner `AnchorPane`.
* **Content:** A `Label` centered saying "MAP VIEW PLACEHOLDER".
* **Comment Marker:** `<!-- STREET VIEW LIBRARY PLACEHOLDER - INSERT MAP COMPONENT HERE -->`

---

## 🛡️ 4. Admin Suite (`src/main/resources/AdminUI/`)

### 4.1 AdminLogin
**File:** `AdminLogin.fxml`
* **Layout:** `VBox` (Centered). Contains a `VBox` card (width 400, `.neon-card`).
* **Header:** `Label` "BOGO ADMIN" (`.header-text`).
* **Fields:**
    * `TextField` `txt_email` (`.neon-text-field`). Validation: email regex.
    * `PasswordField` `txt_password` (`.neon-text-field`). Validation: max 8 chars.
* **Buttons:** `btn_login` (`.button-green`), `btn_back` (`.button-purple`).
* **Marker:** `// TODO: Auth Logic - Validate inputs and route to AdminShell.fxml`

### 4.2 AdminShell
**File:** `AdminShell.fxml`
* **Layout:** `BorderPane`.
* **Top:** `HBox` Header with "BOGO" label on the left.
* **Left:** `VBox` Nav Panel (`.nav-panel`). Buttons: `btn_nav_dashboard`, `btn_nav_analytics`, `btn_nav_alerts`, `btn_nav_add_resource`.
* **Center:** `StackPane` `content_area` (This is where sub-pages are injected).
* **Marker:** `// TODO: Navigation Logic - Update style of active button and load respective FXML into content_area.`

### 4.3 Admin_Dashboard
**File:** `Admin_Dashboard.fxml`
* **Layout:** `GridPane` (2 columns).
* **Col 1 (Alerts):** `VBox` with a `ListView` `list_system_alerts` (Scrollable).
* **Col 2 (Map):** `StackPane` `map_container`. (Include `MapView_ALL.fxml` here).
* **Marker:** `// TODO: Fetch current Message objects (Alerts) from backend.`

### 4.4 Admin_Analytics
**File:** `Admin_Analytics.fxml`
* **Layout:** `VBox` containing 2 `HBox` rows. Uses `StackPane` as root to allow an overlay.
* **Top Row:** Two `.neon-card` boxes for `lbl_total_passengers` and `lbl_total_revenue`.
* **Bottom Row:** Two boxes. 
    * Box 1: `txt_search_employee`, `ListView` `list_employees`.
    * Box 2: `txt_search_driver`, `ListView` `list_drivers`.
* **Overlay:** `AnchorPane` `pane_detail_overlay` (hidden by default). Shows `lbl_detail_name`, `lbl_detail_id`.
* **Marker:** `// TODO: Search Filter Logic - Mock data for preview, link to DB for production. Toggle pane_detail_overlay visibility on list click.`

### 4.5 Admin_ResolveAlert
**File:** `Admin_ResolveAlert.fxml`
* **Layout:** `StackPane` root (for overlay capability). Main content: `VBox` with `ListView` `list_active_alerts`.
* **List Items:** Each item has a `Label` (type/time) and `btn_resolve` (`.button-green`).
* **Overlay (Resolution Dialog):** `VBox` `pane_resolution_dialog` (hidden by default). Contains `btn_assign_bus`, `btn_assign_driver`.
* **Marker:** `// TODO: Backend Resource Check. If (resourceAvailable) { show "Resolved" fade-out toast, remove from list } else { show Error }`

### 4.6 Admin_AddResource
**File:** `Admin_AddResource.fxml`
* **Layout:** `HBox` (Centered, Spacing 30).
* **Box A (Bus):** `.neon-card`. `txt_bus_number` (Regex: [A-Z]{3}-[0-9]{3}), `txt_bus_company`, `txt_bus_year` (4 digits), `choice_max_capacity` (30 or 50). `btn_add_bus` (`.button-green`).
    * `// BACKEND: POST /api/resources/bus - Check for duplicate Bus Number.`
* **Box B (Driver):** `.neon-card`. `txt_driver_fname`, `txt_driver_lname`, `txt_driver_cnic` (13 digits), `txt_driver_email`. `btn_add_driver` (`.button-purple`).
    * `// BACKEND: POST /api/resources/driver - Return generated ID/Password.`

---

## 🚌 5. Driver Suite (`src/main/resources/DriverUI/`)

### 5.1 DriverSignIn
**File:** `DriverSignIn.fxml`
* **Layout:** Similar to AdminLogin.
* **Fields:** `txt_driver_id` (Regex: ABC-1234-1234), `txt_password`.
* **Button:** `btn_driver_login` (`.button-green`).
* **Marker:** `// TODO: Auth Logic - Validate Driver ID format and verify with backend.`

### 5.2 DriverShell
**File:** `DriverShell.fxml`
* **Layout:** `BorderPane` (Same structure as AdminShell).
* **Header:** "BOGO DRIVER".
* **Nav Buttons:** `btn_nav_dashboard`, `btn_nav_send_alert`, `btn_nav_view_route`, `btn_nav_bus_metrics`.

### 5.3 Driver_Dashboard
**File:** `Driver_Dashboard.fxml`
* **Layout:** `HBox` or `SplitPane`.
* **Left/Main:** `StackPane` `map_placeholder` (include map).
* **Right:** `VBox` with `ListView` `list_boarding_passengers`.
* **Marker:** `// TODO: Refresh list every time a passenger scans in/out.`

### 5.4 Driver_SendAlerts
**File:** `Driver_SendAlerts.fxml`
* **Layout:** `VBox` centered (`.neon-card`).
* **Fields:**
    * `ToggleButtonGroup` (High / Low Priority).
    * Toggle Switch (Bus / Driver Type).
    * `TextArea` `txt_alert_message` (`.neon-text-area`).
* **Button:** `btn_send_message` (`.button-purple`).
* **Marker:** `// TODO: Create 'Message' object. SEND to Admin_ResolveAlert queue.`

### 5.5 Driver_ViewRoute
**File:** `Driver_ViewRoute.fxml`
* **Layout:** `AnchorPane` (Full screen). Includes MapView placeholder.
* **Marker:** `/* GOOGLE MAPS / LEAFLET API - LOAD PRE-DEFINED ROUTE POLYLINE HERE */`

### 5.6 Driver_BusMetrics
**File:** `Driver_BusMetrics.fxml`
* **Layout:** `HBox` centered.
* **Content:** 3 `VBox` containers.
    * `ProgressIndicator` `gauge_tyres`.
    * `ProgressIndicator` `gauge_engine`.
    * `ProgressIndicator` `gauge_chassis`.
* **Note:** Use CSS to make these circular and apply color based on health (Green/Yellow/Red).
* **Marker:** `// TODO: Sync with Bus hardware sensors/Mock data.`

---

## 👤 6. Passenger Suite (`src/main/resources/PassengerUI/`)

### 6.1 PassengerLogin_SignUp
**File:** `PassengerLogin_SignUp.fxml`
* **Layout:** `VBox` root. Central `.neon-card` with an `HBox` at the top holding 2 toggle buttons: `btn_show_login`, `btn_show_signup`.
* **Login Pane (`VBox`):** `txt_login_email`, `txt_login_password`, `btn_login`.
* **Signup Pane (`VBox` hidden by default):** `txt_fname`, `txt_lname`, `txt_signup_email`, `pf_password`, `pf_confirm_password`, `btn_signup`.
* **Marker:** `// TODO: Validation - Passwords match. Email unique.`

### 6.2 PassengerShell
**File:** `PassengerShell.fxml`
* **Layout:** `BorderPane` (Same as AdminShell).
* **Header:** "BOGO PASSENGER".
* **Nav Buttons:** `btn_nav_dashboard`, `btn_nav_book`, `btn_nav_multi_stop`.

### 6.3 Passenger_Dashboard
**File:** `Passenger_Dashboard.fxml`
* **Layout:** `StackPane` root (for overlay). Inner `HBox`.
* **Left:** `StackPane` `map_box`.
* **Right:** `ScrollPane` containing `VBox` `box_active_bookings`. Each item is a `.neon-card`.
* **Overlay (Sub-menu):** Clicking a booking opens `pane_cancel_ride`. Contains `btn_cancel_ride` (Red), `btn_go_back` (Purple).
* **Marker:** `// TODO: BACKEND - Send cancellation request; trigger fading notification.`

### 6.4 Passenger_BookRide
**File:** `Passenger_BookRide.fxml`
* **Layout:** `HBox`.
* **Left:** Map container.
* **Right:** Form `.neon-card`. Includes `ComboBox` `combo_stop_1`, `ComboBox` `combo_stop_2` (must support typing/filtering). `btn_confirm_booking` (`.button-green`).
* **Marker:** `// TODO: BACKEND - Send Stop IDs; receive ETA and Price.`

### 6.5 Passenger_MultiStopBooking
**File:** `Passenger_MultiStopBooking.fxml`
* **Layout:** `HBox`.
* **Left:** Map container.
* **Right:** `VBox`.
    * **Step 1:** `TextField` `txt_stop_count` (Numeric), `btn_generate_inputs`.
    * **Step 2:** `ScrollPane` `scroll_dynamic_stops` containing generated `ComboBox` fields.
    * **Action:** `btn_book_multi` (`.button-purple`).
* **Marker:** `// TODO: Logic - Iterate through generated ComboBoxes to extract IDs.`

---

## 🚀 7. Java Launcher
**File:** `src/main/java/org/BOGO/UIPreview.java`
* **Task:** Write a standard JavaFX `Application` class.
* **Logic:** Load `Main.fxml` as the starting scene. Ensure `NeonTheme.css` is applied to the Scene.
* Include basic boilerplate to launch the JavaFX app.
