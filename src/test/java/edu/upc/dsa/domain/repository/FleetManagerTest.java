package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;
import edu.upc.dsa.domain.models.Position;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FleetManagerTest {
	private final FleetManager manager = FleetManagerImpl.getInstance();

	@Test
	public void addDrone() {
		String droneId = uniqueId("drone");
		manager.addDrone(new Drone(droneId, "Test drone", "Test manufacturer", "Test model"));

		boolean found = false;
		for (Drone drone : manager.listDronesByFlightHours()) {
			if (droneId.equals(drone.getId())) {
				found = true;
			}
		}
		assertTrue(found);
	}

	@Test
	public void addPilot() {
		String pilotId = uniqueId("pilot");
		manager.addPilot(new Pilot(pilotId, "Test", "Pilot"));

		boolean found = false;
		for (Pilot pilot : manager.listPilotsByFlightHours()) {
			if (pilotId.equals(pilot.getId())) {
				found = true;
			}
		}
		assertTrue(found);
	}

	@Test
	public void createFlightPlan() {
		FlightPlan plan = newFlightPlan();
		FlightPlan created = manager.createFlightPlan(plan);

		assertNotNull(created.getId());
		assertEquals(plan.getDroneId(), created.getDroneId());
		assertEquals(plan.getPilotId(), created.getPilotId());
	}

	@Test
	public void listFlightPlansByDrone() {
		FlightPlan created = manager.createFlightPlan(newFlightPlan());
		List<FlightPlan> plans = manager.listFlightPlansByDrone(created.getDroneId());

		assertEquals(1, plans.size());
		assertEquals(created.getId(), plans.get(0).getId());
	}

	@Test
	public void repairNextDroneUsesLastStoredDroneFirst() {
		String firstDroneId = uniqueId("drone");
		String secondDroneId = uniqueId("drone");
		manager.addDrone(new Drone(firstDroneId, "First drone", "Test manufacturer", "Test model"));
		manager.addDrone(new Drone(secondDroneId, "Second drone", "Test manufacturer", "Test model"));
		manager.sendDroneToMaintenance(firstDroneId);
		manager.sendDroneToMaintenance(secondDroneId);

		assertEquals(secondDroneId, manager.repairNextDrone().getId());
		assertEquals(firstDroneId, manager.repairNextDrone().getId());
	}

	private FlightPlan newFlightPlan() {
		String droneId = uniqueId("drone");
		String pilotId = uniqueId("pilot");
		manager.addDrone(new Drone(droneId, "Test drone", "Test manufacturer", "Test model"));
		manager.addPilot(new Pilot(pilotId, "Test", "Pilot"));

		FlightPlan plan = new FlightPlan();
		plan.setDroneId(droneId);
		plan.setPilotId(pilotId);
		plan.setDateTime(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());
		plan.setEstimatedHours(1.0);
		List<Position> positions = new ArrayList<Position>();
		positions.add(new Position(41.275, 1.988));
		plan.setPositions(positions);
		return plan;
	}

	private String uniqueId(String prefix) {
		return prefix + "-" + System.nanoTime();
	}
}