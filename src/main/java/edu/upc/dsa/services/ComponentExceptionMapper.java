package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.ErrorDTO;
import edu.upc.dsa.exceptions.ComponentException;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ComponentExceptionMapper implements ExceptionMapper<ComponentException> {
	private static final Logger logger = Logger.getLogger(ComponentExceptionMapper.class);

	@Override
	public Response toResponse(ComponentException exception) {
		logger.error("REST request failed with status " + exception.getStatus() + ": " + exception.getMessage());
		ErrorDTO error = new ErrorDTO(exception.getStatus(), exception.getMessage());
		return Response.status(exception.getStatus()).entity(error).build();
	}
}