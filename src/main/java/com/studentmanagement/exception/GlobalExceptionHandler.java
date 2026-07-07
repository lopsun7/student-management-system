package com.studentmanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
		ResourceNotFoundException exception,
		HttpServletRequest request
	) {
		return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		for (FieldError error : exception.getBindingResult().getFieldErrors()) {
			validationErrors.put(error.getField(), error.getDefaultMessage());
		}

		return buildResponse(
			HttpStatus.BAD_REQUEST,
			"Validation failed",
			request.getRequestURI(),
			validationErrors
		);
	}

	@ExceptionHandler(DownstreamRequestFailedException.class)
	public ResponseEntity<ApiErrorResponse> handleDownstreamRequestFailure(
		DownstreamRequestFailedException exception,
		HttpServletRequest request
	) {
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> handleAuthenticationFailure(
		AuthenticationException exception,
		HttpServletRequest request
	) {
		return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", request.getRequestURI(), null);
	}

	private ResponseEntity<ApiErrorResponse> buildResponse(
		HttpStatus status,
		String message,
		String path,
		Map<String, String> validationErrors
	) {
		ApiErrorResponse errorResponse = new ApiErrorResponse(
			LocalDateTime.now(),
			status.value(),
			status.getReasonPhrase(),
			message,
			path,
			validationErrors
		);

		return ResponseEntity.status(status).body(errorResponse);
	}
}
