# BOGO - Graph-Based Map Visualization & Pathfinding Test Blueprint

**System Prompt for AI Agent:**
You are to implement a sophisticated Graph Visualization system in JavaFX. The goal is to bridge the existing backend Domain models and Service logic with a visual "Live Map" interface. You must analyze the existing logic in the specified directories and create a standalone test environment to verify that the graph renders correctly and that the path-building results are visually reflected on the UI.

---

## 🛠️ 1. Source Code Analysis Phase
Before writing the UI code, the agent must read and understand:
1.  **Domain Models:** `src/main/java/org/BOGO/Domain/Transport/Stop.java` and `Route.java`.
    * Identify how `Location` (X, Y or Lat/Lon) is stored.
    * Identify the `connections` array/list in the `Stop` class.
2.  **Service Logic:** `src/main/java/org/BOGO/Service/PathBuildingService.java`.
    * Understand the method signature for finding a path (e.g., `findShortestPath(Stop start, Stop end)`).
3.  **Demo Data:** `src/main/java/org/BOGO/Test.java`.
    * Use the existing graph initialization code (how stops are linked) to populate the test map.

---

## 🎨 2. Visual Requirements (The "Neon Graph" Aesthetic)
* **Static Edges (Paths):** All connections between stops must be rendered as **Curved Lines** (using `CubicCurve` or `QuadCurve`).
    * *Default Style:* Stroke color: **Grey** (`#4a4a4a`), Stroke width: 2px, Opacity: 0.6.
* **Active Path:** When a path is found by the `PathBuildingService`:
    * *Active Style:* Stroke color: **Neon Blue** (`#00f2ff`), Stroke width: 4px, Opacity: 1.0. Apply a `Glow` effect.
* **Stops (Nodes):**
    * Rendered as `Circle` objects or custom `StackPane` nodes.
    * *Style:* Neon Cyan border, Dark Blue fill.
    * *Interactivity:* Add a `Tooltip` to every stop showing: `Stop Name`, `ID`, and `Location Coordinates`.
    * *Live Objects:* Each node should have `setOnMouseEntered` and `setOnMouseExited` handlers to scale up slightly and glow when hovered.

---

## 📂 3. Files to Generate

### 3.1 TestMap.fxml
* **Directory:** `src/main/resources/TestMap.fxml`
* **Root:** `AnchorPane` or `BorderPane`.
* **Main Component:** A custom `Pane` ID: `mapCanvas`. This pane will serve as the coordinate space for the graph.
* **UI Controls:** * A Top `HBox` containing two `ComboBox<Stop>` (Source and Destination).
    * A `Button` ID: `btn_find_path` (Style: `.button-green`).

### 3.2 TestMap.java (Controller & Launcher)
* **Directory:** `src/main/java/org/BOGO/TestMap.java`
* **Functionality:**
    1.  **Initialize:** Load data from the graph setup found in `Test.java`.
    2.  **Render Graph:** Loop through all `Stop` objects. For each stop, draw a `Circle`. For each connection, draw a `CubicCurve` to the destination stop.
    3.  **Coordinate Mapping:** If coordinates are high-value (Lat/Lon), implement a mapping function to translate them to the `mapCanvas` dimensions.
    4.  **Pathfinding Trigger:** * On `btn_find_path` click, call `PathBuildingService`.
        * The service returns a `List<Stop>` or `Route`.
        * Identify the `CubicCurve` segments that connect the stops in the returned list.
        * Transition those segments from **Grey** to **Neon Blue** using a `StrokeTransition` or direct CSS update.

---

## 🧩 4. Technical Implementation Notes for Agent
* **Z-Order:** Ensure `Lines` (Edges) are added to the Pane children first, and `Circles` (Nodes) are added last so that stops appear "on top" of the paths.
* **Curvature Calculation:** To create a "curved" path between two points (x1, y1) and (x2, y2), calculate a control point that is offset from the midpoint to create an arc.
* **Binding:** Bind the `startXProperty` and `startYProperty` of the curves to the `centerXProperty` of the Stop circles. This makes the graph "live"—if a stop node moves, the curved path stretches with it.

---

## 🚀 5. Execution Step
Run the `TestMap.java` application. The result should show a dark-themed window with a grey network of curved lines. Selecting two stops and clicking "Find Path" should cause the connecting curves to light up in Neon Blue.
