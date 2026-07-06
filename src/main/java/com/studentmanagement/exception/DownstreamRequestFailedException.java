package com.studentmanagement.exception;

public class DownstreamRequestFailedException extends RuntimeException {

	public DownstreamRequestFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
