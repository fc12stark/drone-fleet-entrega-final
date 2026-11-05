# Drone fleet pilot

Java 8 Maven project for the DSA assignment. The component is the `FleetManager` interface and the Singleton `FleetManagerImpl`; the REST service uses that same component. Data is kept in memory and is lost when the process stops.

## Current status

Parts II and III are implemented. The five component tests pass, and the REST routes have been tested through Swagger UI.

## Part II: component operations

`FleetManager` exposes operations to add drones and pilots, list both groups by decreasing flight hours, put a drone in maintenance, repair the drone nearest the door, create a flight plan, and list plans by pilot or drone. The implementation uses maps for lookup and a LIFO stack for maintenance. All public operations trace their entry and exit with Log4j; errors are logged and surfaced as `ComponentException`.

JUnit tests cover adding a drone, adding a pilot, creating a plan, listing plans for a drone, and repairing the last drone stored in the maintenance stack. Run them with `mvn test`.

## Part III: REST API

Run the service with `mvn compile exec:java`. Base URL: `http://localhost:8080/dsaApp`. Open Swagger UI at `http://localhost:8080/swagger/index.html`; it reads the Swagger specification from `/dsaApp/swagger.json` and can send requests to the four endpoints.

| Method | Route | Request / response | Success |
|---|---|---|---|
| POST | `/drones` | JSON `Drone`; returns the created drone | 201 |
| POST | `/pilots` | JSON `Pilot`; returns the created pilot | 201 |
| POST | `/flight-plans` | JSON `FlightPlan` (omit `id`; include `droneId`, `pilotId`, `dateTime`, `estimatedHours`, and `positions`); returns the created plan | 201 |
| GET | `/drones/{droneId}/flight-plans` | Returns a JSON list of the drone's flight plans | 200 |

For example, `dateTime` uses ISO local date/time such as `2026-11-12T14:30`; a position has `latitude` and `longitude`. Error responses contain `status` and `message`: 400 for invalid input, 404 for an unknown drone or pilot, and 409 for duplicate IDs, maintenance state, or an overlapping reservation.

## Current limitations

- Flight hours are calculated by adding the estimated duration when a plan is created. The component does not record completed flights.
- The REST API exposes four operations: add a drone, add a pilot, create a flight plan, and list plans for a drone. Plan creation includes `pilotId` to check pilot schedule conflicts.
- Flight-plan times are local and have no time zone. Overlap checks apply to plans for the same drone or pilot.
