package edu.upc.dsa.domain.models;

import java.util.ArrayList;
import java.util.List;

public class FlightPlan {
	private String id;
	private String droneId;
	private String pilotId;
	private String dateTime;
	private double estimatedHours;
	private List<Position> positions;

	public FlightPlan() {
		this.positions = new ArrayList<Position>();
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getDroneId() { return droneId; }
	public void setDroneId(String droneId) { this.droneId = droneId; }
	public String getPilotId() { return pilotId; }
	public void setPilotId(String pilotId) { this.pilotId = pilotId; }
	public String getDateTime() { return dateTime; }
	public void setDateTime(String dateTime) { this.dateTime = dateTime; }
	public double getEstimatedHours() { return estimatedHours; }
	public void setEstimatedHours(double estimatedHours) { this.estimatedHours = estimatedHours; }
	public List<Position> getPositions() { return positions; }
	public void setPositions(List<Position> positions) { this.positions = positions; }

	@Override
	public String toString() {
		return "FlightPlan{id='" + id + "', droneId='" + droneId + "', pilotId='" + pilotId
				+ "', dateTime='" + dateTime + "', estimatedHours=" + estimatedHours
				+ ", positions=" + positions + '}';
	}
}