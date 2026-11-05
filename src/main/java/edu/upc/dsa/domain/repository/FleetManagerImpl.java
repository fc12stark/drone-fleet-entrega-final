package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;
import edu.upc.dsa.domain.models.Position;
import edu.upc.dsa.exceptions.ComponentException;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class FleetManagerImpl implements FleetManager {
	private static final Logger logger = Logger.getLogger(FleetManagerImpl.class);
	private static final FleetManagerImpl instance = new FleetManagerImpl();

	private final Map<String, Drone> drones;
	private final Map<String, Pilot> pilots;
	private final List<FlightPlan> flightPlans;
	private final Stack<String> maintenanceStack;
	private final Set<String> dronesInMaintenance;
	private int nextFlightPlanId;

	private FleetManagerImpl() {
		this.drones = new HashMap<String, Drone>();
		this.pilots = new HashMap<String, Pilot>();
		this.flightPlans = new ArrayList<FlightPlan>();
		this.maintenanceStack = new Stack<String>();
		this.dronesInMaintenance = new HashSet<String>();
		this.nextFlightPlanId = 1;
	}

	public static FleetManagerImpl getInstance() {
		return instance;
	}

	@Override
	public synchronized void addDrone(Drone drone) {
		logger.info("addDrone start: drone=" + drone);
		try {
			if (drone == null || isBlank(drone.getId()) || isBlank(drone.getName())
					|| isBlank(drone.getManufacturer()) || isBlank(drone.getModel())) {
				throw failure(400, "Drone fields must not be empty");
			}
			if (drones.containsKey(drone.getId())) {
				throw failure(409, "Drone already exists: " + drone.getId());
			}
			Drone storedDrone = copyDrone(drone);
			storedDrone.setFlightHours(0.0);
			storedDrone.setInMaintenance(false);
			drones.put(storedDrone.getId(), storedDrone);
			logger.info("addDrone result: " + storedDrone);
		} finally {
			logger.info("addDrone end: drone=" + drone);
		}
	}

	@Override
	public synchronized void addPilot(Pilot pilot) {
		logger.info("addPilot start: pilot=" + pilot);
		try {
			if (pilot == null || isBlank(pilot.getId()) || isBlank(pilot.getName())
					|| isBlank(pilot.getSurname())) {
				throw failure(400, "Pilot fields must not be empty");
			}
			if (pilots.containsKey(pilot.getId())) {
				throw failure(409, "Pilot already exists: " + pilot.getId());
			}
			Pilot storedPilot = copyPilot(pilot);
			storedPilot.setFlightHours(0.0);
			pilots.put(storedPilot.getId(), storedPilot);
			logger.info("addPilot result: " + storedPilot);
		} finally {
			logger.info("addPilot end: pilot=" + pilot);
		}
	}

	@Override
	public synchronized List<Drone> listDronesByFlightHours() {
		logger.info("listDronesByFlightHours start");
		try {
			List<Drone> result = new ArrayList<Drone>();
			for (Drone drone : drones.values()) {
				result.add(copyDrone(drone));
			}
			Collections.sort(result, new Comparator<Drone>() {
				@Override
				public int compare(Drone first, Drone second) {
					return Double.compare(second.getFlightHours(), first.getFlightHours());
				}
			});
			logger.info("listDronesByFlightHours result: " + result);
			return result;
		} finally {
			logger.info("listDronesByFlightHours end");
		}
	}

	@Override
	public synchronized List<Pilot> listPilotsByFlightHours() {
		logger.info("listPilotsByFlightHours start");
		try {
			List<Pilot> result = new ArrayList<Pilot>();
			for (Pilot pilot : pilots.values()) {
				result.add(copyPilot(pilot));
			}
			Collections.sort(result, new Comparator<Pilot>() {
				@Override
				public int compare(Pilot first, Pilot second) {
					return Double.compare(second.getFlightHours(), first.getFlightHours());
				}
			});
			logger.info("listPilotsByFlightHours result: " + result);
			return result;
		} finally {
			logger.info("listPilotsByFlightHours end");
		}
	}

	@Override
	public synchronized void sendDroneToMaintenance(String droneId) {
		logger.info("sendDroneToMaintenance start: droneId=" + droneId);
		try {
			Drone drone = drones.get(droneId);
			if (drone == null) {
				throw failure(404, "Drone not found: " + droneId);
			}
			if (dronesInMaintenance.contains(droneId)) {
				throw failure(409, "Drone is already in maintenance: " + droneId);
			}
			maintenanceStack.push(droneId);
			dronesInMaintenance.add(droneId);
			drone.setInMaintenance(true);
			logger.info("sendDroneToMaintenance result: droneId=" + droneId);
		} finally {
			logger.info("sendDroneToMaintenance end: droneId=" + droneId);
		}
	}

	@Override
	public synchronized Drone repairNextDrone() {
		logger.info("repairNextDrone start");
		try {
			if (maintenanceStack.empty()) {
				throw failure(409, "There are no drones waiting for maintenance");
			}
			String droneId = maintenanceStack.pop();
			Drone drone = drones.get(droneId);
			dronesInMaintenance.remove(droneId);
			drone.setInMaintenance(false);
			Drone result = copyDrone(drone);
			logger.info("repairNextDrone result: " + result);
			return result;
		} finally {
			logger.info("repairNextDrone end");
		}
	}

	@Override
	public synchronized FlightPlan createFlightPlan(FlightPlan flightPlan) {
		logger.info("createFlightPlan start: flightPlan=" + flightPlan);
		try {
			if (flightPlan == null || isBlank(flightPlan.getDroneId()) || isBlank(flightPlan.getPilotId())
					|| isBlank(flightPlan.getDateTime())) {
				throw failure(400, "Flight plan fields must not be empty");
			}
			if (Double.isNaN(flightPlan.getEstimatedHours()) || Double.isInfinite(flightPlan.getEstimatedHours())
					|| flightPlan.getEstimatedHours() <= 0.0) {
				throw failure(400, "Estimated flight hours must be greater than zero");
			}
			if (flightPlan.getPositions() == null || flightPlan.getPositions().isEmpty()) {
				throw failure(400, "At least one flight position is required");
			}
			for (Position position : flightPlan.getPositions()) {
				if (position == null || Double.isNaN(position.getLatitude()) || Double.isInfinite(position.getLatitude())
						|| position.getLatitude() < -90.0 || position.getLatitude() > 90.0
						|| Double.isNaN(position.getLongitude()) || Double.isInfinite(position.getLongitude())
						|| position.getLongitude() < -180.0 || position.getLongitude() > 180.0) {
					throw failure(400, "Flight positions must have valid latitude and longitude");
				}
			}

			Drone drone = drones.get(flightPlan.getDroneId());
			if (drone == null) {
				throw failure(404, "Drone not found: " + flightPlan.getDroneId());
			}
			if (dronesInMaintenance.contains(drone.getId())) {
				throw failure(409, "Drone is in maintenance: " + drone.getId());
			}
			Pilot pilot = pilots.get(flightPlan.getPilotId());
			if (pilot == null) {
				throw failure(404, "Pilot not found: " + flightPlan.getPilotId());
			}

			LocalDateTime start;
			try {
				start = LocalDateTime.parse(flightPlan.getDateTime());
			} catch (DateTimeParseException exception) {
				throw failure(400, "dateTime must use ISO format, for example 2026-11-05T14:30");
			}
			LocalDateTime end = endTime(start, flightPlan.getEstimatedHours());
			for (FlightPlan existing : flightPlans) {
				LocalDateTime existingStart = LocalDateTime.parse(existing.getDateTime());
				LocalDateTime existingEnd = endTime(existingStart, existing.getEstimatedHours());
				boolean sameDrone = existing.getDroneId().equals(drone.getId());
				boolean samePilot = existing.getPilotId().equals(pilot.getId());
				boolean overlaps = start.isBefore(existingEnd) && existingStart.isBefore(end);
				if (overlaps && (sameDrone || samePilot)) {
					throw failure(409, "Drone or pilot already has a flight plan in this time interval");
				}
			}

			FlightPlan storedPlan = copyFlightPlan(flightPlan);
			storedPlan.setId("FP-" + nextFlightPlanId++);
			flightPlans.add(storedPlan);
			drone.setFlightHours(drone.getFlightHours() + storedPlan.getEstimatedHours());
			pilot.setFlightHours(pilot.getFlightHours() + storedPlan.getEstimatedHours());
			FlightPlan result = copyFlightPlan(storedPlan);
			logger.info("createFlightPlan result: " + result);
			return result;
		} finally {
			logger.info("createFlightPlan end: flightPlan=" + flightPlan);
		}
	}

	@Override
	public synchronized List<FlightPlan> listFlightPlansByPilot(String pilotId) {
		logger.info("listFlightPlansByPilot start: pilotId=" + pilotId);
		try {
			if (!pilots.containsKey(pilotId)) {
				throw failure(404, "Pilot not found: " + pilotId);
			}
			List<FlightPlan> result = new ArrayList<FlightPlan>();
			for (FlightPlan flightPlan : flightPlans) {
				if (pilotId.equals(flightPlan.getPilotId())) {
					result.add(copyFlightPlan(flightPlan));
				}
			}
			logger.info("listFlightPlansByPilot result: " + result);
			return result;
		} finally {
			logger.info("listFlightPlansByPilot end: pilotId=" + pilotId);
		}
	}

	@Override
	public synchronized List<FlightPlan> listFlightPlansByDrone(String droneId) {
		logger.info("listFlightPlansByDrone start: droneId=" + droneId);
		try {
			if (!drones.containsKey(droneId)) {
				throw failure(404, "Drone not found: " + droneId);
			}
			List<FlightPlan> result = new ArrayList<FlightPlan>();
			for (FlightPlan flightPlan : flightPlans) {
				if (droneId.equals(flightPlan.getDroneId())) {
					result.add(copyFlightPlan(flightPlan));
				}
			}
			logger.info("listFlightPlansByDrone result: " + result);
			return result;
		} finally {
			logger.info("listFlightPlansByDrone end: droneId=" + droneId);
		}
	}

	private LocalDateTime endTime(LocalDateTime start, double estimatedHours) {
		double durationNanos = estimatedHours * 3600000000000.0;
		if (Double.isInfinite(durationNanos) || durationNanos < 1.0 || durationNanos > Long.MAX_VALUE) {
			throw failure(400, "Estimated flight duration is outside the supported range");
		}
		try {
			return start.plusNanos((long) durationNanos);
		} catch (DateTimeException exception) {
			throw failure(400, "Flight plan date and duration exceed the supported date range");
		}
	}

	private ComponentException failure(int status, String message) {
		logger.error(message);
		return new ComponentException(status, message);
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private Drone copyDrone(Drone original) {
		Drone copy = new Drone(original.getId(), original.getName(), original.getManufacturer(), original.getModel());
		copy.setFlightHours(original.getFlightHours());
		copy.setInMaintenance(original.isInMaintenance());
		return copy;
	}

	private Pilot copyPilot(Pilot original) {
		Pilot copy = new Pilot(original.getId(), original.getName(), original.getSurname());
		copy.setFlightHours(original.getFlightHours());
		return copy;
	}

	private FlightPlan copyFlightPlan(FlightPlan original) {
		FlightPlan copy = new FlightPlan();
		copy.setId(original.getId());
		copy.setDroneId(original.getDroneId());
		copy.setPilotId(original.getPilotId());
		copy.setDateTime(original.getDateTime());
		copy.setEstimatedHours(original.getEstimatedHours());
		List<Position> positions = new ArrayList<Position>();
		if (original.getPositions() != null) {
			for (Position position : original.getPositions()) {
				positions.add(new Position(position.getLatitude(), position.getLongitude()));
			}
		}
		copy.setPositions(positions);
		return copy;
	}
}