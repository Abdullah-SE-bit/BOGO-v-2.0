# BOGO — Smart Public Bus Transport System
## Non-Technical Context Document

---

## 1. Project Overview

**BOGO** is a smart, application-based public bus transport system inspired by the concept of cab-booking platforms (such as Uber or Careem), but applied to the domain of public bus transportation. The core problem BOGO solves is the inefficiency and opacity of traditional public bus systems — where buses are overcrowded with no advance booking, breakdowns go unreported through any digital channel, and passengers have no way to plan multi-leg journeys intelligently.

BOGO creates a structured, role-based digital ecosystem where every participant — the **Admin**, the **Driver**, and the **Passenger** — has their own dedicated interface and a defined set of responsibilities and capabilities. The system is designed to simulate a real, living transport network, with buses moving in real time on a map, stops being activated or blocked dynamically, and routes being managed centrally by administrators.

---

## 2. The Problem Being Solved

Public bus transport in most cities suffers from the following issues:

- **No booking system**: Passengers cannot reserve seats or plan ahead. Buses are first-come, first-served and frequently overcrowded.
- **No breakdown reporting**: If a bus breaks down or a driver is incapacitated, there is no fast, digital reporting mechanism. Reports happen over phone calls or are not made at all.
- **No route intelligence**: Passengers cannot find the shortest path between two points if it involves changing buses. They must rely on personal knowledge or word of mouth.
- **No real-time visibility**: There is no way to know where a bus is, how full it is, or when it will arrive at a given stop.
- **No centralized management**: Admins/operators have no digital tools to assign drivers to routes, add new buses, or track system health.

BOGO addresses all of these problems through a unified three-actor digital platform.

---

## 3. The Three Actors

### 3.1 Admin

The Admin is the central authority of the entire BOGO system. The Admin does not interact with buses or stops physically — instead, the Admin manages the entire system digitally through a dedicated Admin panel.

**Admin Responsibilities:**

- **Add Resources**: The Admin can add new drivers to the system and add new buses to the fleet. Drivers cannot self-register — they must be added by the Admin. This ensures controlled onboarding of drivers.
- **Add Stops and Routes**: The Admin can define new bus stops (with their geographic location and connections to other stops) and can create new bus routes (by selecting a sequence of connected stops). This gives the Admin full control over the physical layout of the transport network.
- **Assign Routes to Drivers**: The Admin can assign a specific route to a specific driver. When this assignment is made, the driver receives it in their own panel. If a driver has not been assigned a route, the system will automatically assign any available route to the driver.
- **View System Analytics**: The Admin can see a live dashboard showing the total number of routes, buses, drivers, and passengers in the system, along with searchable lists of all these entities.
- **Resolve Issues (Alerts)**: Drivers can send alerts to the system when there is a problem (bus breakdown, driver emergency, etc.). The Admin sees all unresolved alerts in a list and must click on one to resolve it. Upon resolution, the Admin sends a response/resolution message, which is delivered as a notification to the relevant driver's panel.

### 3.2 Driver

The Driver is the person operating a bus on a specific route. Drivers are added to the system by the Admin — they cannot self-register.

**Driver Responsibilities and Features:**

- **View Assigned Route on Map**: The driver has a map view that shows their specifically assigned route, with all stops highlighted along it. The driver can see exactly which stops are on their route.
- **Mark Stops as Blocked**: On the map, each stop on the driver's route has a tooltip. Through this tooltip, the driver can mark a stop as "blocked" (e.g., due to road construction or an obstruction). When a stop is marked blocked, the system automatically reroutes around it — finding the next appropriate adjacent stop — without breaking the continuity of the route.
- **Send Alerts**: If the bus breaks down or the driver has an emergency, they can send an alert to the Admin. The alert has two priority levels: **High Priority** and **Low Priority**, and two types: **Bus Down** or **Driver Down**. This alert appears in the Admin's panel as a notification requiring resolution.
- **View Bus Metrics**: The driver can view the health metrics of their assigned bus, including **tyre health**, **engine health**, and **chassis health**. Based on these metrics, the driver can judge whether the bus is reliable or needs to be reported as broken down.
- **Receive Notifications**: When the Admin resolves an alert sent by the driver, the driver receives a notification in their panel with the Admin's resolution/response.

### 3.3 Passenger

The Passenger is a registered user of the BOGO app who uses buses to travel. Unlike drivers, passengers **can self-register** through the app's sign-up flow.

**Passenger Features:**

- **Sign Up / Sign In**: Passengers can register themselves and log in to the system independently.
- **View Map**: Passengers can view the general map of the entire bus network, showing all routes, stops, and — in real time — the positions of all buses currently operating.
- **Book a Ride (Single Stop)**: The passenger selects a departure stop and a destination stop. The system calculates the optimal path, potentially across multiple routes, and presents it to the passenger. The booking is then saved.
- **Book a Multi-Leg Ride**: The passenger can book a complex journey involving multiple stops and potentially multiple buses. The system handles the route-finding automatically — it tells the passenger which buses to take, where to board, and where to transfer between buses. The system always finds the **shortest possible path** to the destination.
- **Cancel a Booking**: From the main screen, the passenger can view all their current bookings and cancel any of them.
- **See Buses Around Them**: The passenger can see buses near their current location on the map.

---

## 4. The Map — The Heart of the System

The map is arguably the most critical module in BOGO. It is not a static image — it is a **live, dynamic, graph-based representation** of the entire transport network.

### 4.1 What a Stop Is

A **Stop** is a physical location in the city where buses halt to load and unload passengers. Each stop has:
- A name
- A geographic location (coordinates)
- A list of **connections** — other stops that are directly adjacent to it (i.e., a bus can travel directly from this stop to those stops without passing through any intermediate stop)

This connection model means that **stops form a graph** — a network where each node is a stop and each edge is a direct connection between two stops.

### 4.2 What a Route Is

A **Route** is a fixed, ordered sequence of stops that a specific bus will travel along. A route cannot be changed on the fly — it is defined by the Admin. However, the system can **dynamically reroute** around blocked stops while keeping the route logically intact.

### 4.3 What a Path Is

A **Path** is what a passenger books. Unlike a route (which belongs to one bus), a path can span **multiple routes** — meaning the passenger may need to change buses during their journey. The path is automatically computed by the system using shortest-path algorithms on the stop graph.

### 4.4 Map Views

There are multiple views of the map depending on who is looking at it:

| Map View | Who Sees It | What It Shows |
|---|---|---|
| **General Map** | Everyone (Admin, Driver, Passenger) | All routes, all stops, all buses moving in real time |
| **Booking Map** | Passenger | Used for selecting departure and destination stops to book a ride |
| **Admin Marking Map** | Admin | All routes shown; Admin can mark a route inactive or a stop inactive |
| **Driver Route Map** | Driver | Only the driver's assigned route is highlighted; driver can mark stops blocked |

### 4.5 Route Diversion Logic

When a stop is marked **inactive** (by Admin) or **blocked** (by Driver), the route that contains that stop must be diverted. The system uses **graph traversal** to find the best alternative:

1. Identify the stop before the blocked stop and the stop after the blocked stop on the route.
2. Find a stop that is adjacent (directly connected) to **both** the stop before and the stop after the blocked one.
3. If such a stop exists, it becomes the new intermediate stop, replacing the blocked one.
4. If no such single stop exists, expand the search — find a short sub-path that bridges the gap — using standard graph search algorithms (BFS/Dijkstra).

This ensures routes never "break" due to a blocked stop — they always find an alternative.

---

## 5. The Alert and Notification System

The alert/notification system is BOGO's built-in communication channel between drivers and the Admin.

- A **Driver** can generate an **Alert** by selecting a type (Bus Down / Driver Down) and a priority (High / Low).
- The alert is sent through the system to the **Admin**.
- The **Admin** sees the alert in their "Resolve Issues" panel. All unresolved alerts appear in a list.
- The Admin clicks an alert, writes a resolution/response, and resolves it.
- A **Notification** is generated and sent to the relevant **Driver** confirming that the issue has been resolved and relaying the Admin's response.

---

## 6. The Simulation Requirement

BOGO is not a static booking system — it must **simulate a live transport network**. This means:

- Buses must appear to **move** along their routes on the map in real time.
- Each bus has a **simulated travel time** between stops (imaginary/fixed time values used to drive the animation).
- When a bus reaches a stop, it **pauses** for a simulated dwell time (as if loading/unloading passengers), then **resumes** movement.
- The moving bus icons on the map give every user — Admin, Driver, and Passenger — the feeling of watching a live, operating transport system.
- This simulation makes the system feel real and demonstrates the full capability of the platform even in a demo/academic context.

---

## 7. User Roles and Access Control

| Feature | Admin | Driver | Passenger |
|---|---|---|---|
| Self Sign Up | No (added via backend) | No (added by Admin) | Yes |
| Sign In | Yes | Yes | Yes |
| Add Driver | Yes | No | No |
| Add Bus | Yes | No | No |
| Add Stop | Yes | No | No |
| Add Route | Yes | No | No |
| Assign Route to Driver | Yes | No | No |
| View System Analytics | Yes | No | No |
| Resolve Alerts | Yes | No | No |
| Send Alert | No | Yes | No |
| View Bus Metrics | No | Yes | No |
| View Own Route Map | No | Yes | No |
| Mark Stop Blocked | No | Yes | No |
| Book Ride | No | No | Yes |
| Cancel Booking | No | No | Yes |
| View General Map | Yes | Yes | Yes |
| Receive Notifications | No | Yes | No |

---

## 8. Technology Stack (Non-Technical Summary)

- **Backend Logic**: Written in core Java — the brain of the system handling all business rules.
- **User Interface**: Built with JavaFX using FXML layout files and CSS styling (the visual skin is called `NeonTheme.css`).
- **Database**: SQL Server — stores all data about users, routes, stops, bookings, alerts, and trips.
- **Architecture**: The system is built in clean, layered architecture following industry-standard design patterns to ensure it is maintainable, scalable, and robust.

---

## 9. Summary of What is Already Built vs. What Needs Building

### Already Built (UI and Some Services):
- Authentication (Login/Sign Up) — connected to DB and working
- General map base (the map canvas exists)
- Booking map (within passenger UI)
- Admin Shell UI (navigation panel exists)
- Driver UI shell
- Passenger UI shell
- Bus alert sending UI (driver side)
- System analytics UI (partial — drivers and passengers boxes exist)
- Resolve issue UI (admin side)

### To Be Built by Antigravity:
- Complete backend wiring for all services
- MapController (does not exist yet)
- MapRepository (does not exist yet)
- Add Stops / Routes UI page (new FXML page + NeonTheme.css additions)
- Assign Route to Driver UI and feature
- Driver Route Map (map variant showing only the driver's route)
- Admin Marking Map (admin can mark stops/routes inactive)
- Bus metrics fields in database
- System analytics bus box (third box in analytics)
- Real-time bus movement simulation on map
- Stop diversion/rerouting logic
- Full path-building algorithm for multi-leg journeys
- Three separate deployable JAR files (one per actor)
