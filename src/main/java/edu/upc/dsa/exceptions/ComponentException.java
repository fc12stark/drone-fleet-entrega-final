package edu.upc.dsa.exceptions;

public class ComponentException extends RuntimeException {
	private final int status;

	public ComponentException(int status, String message) {
		super(message);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}