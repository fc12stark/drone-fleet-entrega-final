package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;

import java.util.List;

public interface FleetManager {
	void addDrone(Drone drone);
	void addPilot(Pilot pilot);
	List<Drone> listDronesByFlightHours();
	List<Pilot> listPilotsByFlightHours();
	void sendDroneToMaintenance(String droneId);
	Drone repairNextDrone();
	FlightPlan createFlightPlan(FlightPlan flightPlan);
	List<FlightPlan> listFlightPlansByPilot(String pilotId);
	List<FlightPlan> listFlightPlansByDrone(String droneId);
}