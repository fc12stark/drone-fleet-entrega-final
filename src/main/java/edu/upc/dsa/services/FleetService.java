package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.models.ErrorDTO;
import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;
import edu.upc.dsa.domain.repository.FleetManager;
import edu.upc.dsa.domain.repository.FleetManagerImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/", description = "Drone fleet operations")
public class FleetService {
	private static final Logger logger = Logger.getLogger(FleetService.class);
	private final FleetManager manager;

	public FleetService() {
		this.manager = FleetManagerImpl.getInstance();
	}

	@POST
	@Path("/drones")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add a drone", notes = "Registers a drone in the fleet")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Drone created", response = Drone.class),
			@ApiResponse(code = 400, message = "Invalid drone data", response = ErrorDTO.class),
			@ApiResponse(code = 409, message = "Drone identifier already exists", response = ErrorDTO.class)
	})
	public Response addDrone(Drone drone) {
		logger.info("POST /drones start: drone=" + drone);
		manager.addDrone(drone);
		logger.info("POST /drones end: droneId=" + drone.getId());
		return Response.status(Response.Status.CREATED).entity(drone).build();
	}

	@POST
	@Path("/pilots")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add a pilot", notes = "Registers a pilot in the fleet")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Pilot created", response = Pilot.class),
			@ApiResponse(code = 400, message = "Invalid pilot data", response = ErrorDTO.class),
			@ApiResponse(code = 409, message = "Pilot identifier already exists", response = ErrorDTO.class)
	})
	public Response addPilot(Pilot pilot) {
		logger.info("POST /pilots start: pilot=" + pilot);
		manager.addPilot(pilot);
		logger.info("POST /pilots end: pilotId=" + pilot.getId());
		return Response.status(Response.Status.CREATED).entity(pilot).build();
	}

	@POST
	@Path("/flight-plans")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create a flight plan", notes = "The dateTime uses ISO local date/time, for example 2026-11-12T14:30")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Flight plan created", response = FlightPlan.class),
			@ApiResponse(code = 400, message = "Invalid flight plan data", response = ErrorDTO.class),
			@ApiResponse(code = 404, message = "Drone or pilot not found", response = ErrorDTO.class),
			@ApiResponse(code = 409, message = "Drone unavailable or time interval overlaps", response = ErrorDTO.class)
	})
	public Response createFlightPlan(FlightPlan flightPlan) {
		logger.info("POST /flight-plans start: flightPlan=" + flightPlan);
		FlightPlan created = manager.createFlightPlan(flightPlan);
		logger.info("POST /flight-plans end: flightPlanId=" + created.getId());
		return Response.status(Response.Status.CREATED).entity(created).build();
	}

	@GET
	@Path("/drones/{droneId}/flight-plans")
	@ApiOperation(value = "List a drone's flight plans", notes = "Returns all plans assigned to the given drone")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Flight plans", response = FlightPlan.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Drone not found", response = ErrorDTO.class)
	})
	public Response listFlightPlansByDrone(@PathParam("droneId") String droneId) {
		logger.info("GET /drones/" + droneId + "/flight-plans start");
		List<FlightPlan> flightPlans = manager.listFlightPlansByDrone(droneId);
		GenericEntity<List<FlightPlan>> entity = new GenericEntity<List<FlightPlan>>(flightPlans) { };
		logger.info("GET /drones/" + droneId + "/flight-plans end: count=" + flightPlans.size());
		return Response.ok(entity).build();
	}
}