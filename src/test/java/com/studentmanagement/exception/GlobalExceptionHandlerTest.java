package com.studentmanagement.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

	@Mock
	private HttpServletRequest request;

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void shouldReturnNotFoundResponseForMissingResource() {
		when(request.getRequestURI()).thenReturn("/api/v1/students/99");

		ResponseEntity<ApiErrorResponse> response = handler.handleResourceNotFound(
			new ResourceNotFoundException("Student not found"),
			request
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(404);
		assertThat(response.getBody().message()).isEqualTo("Student not found");
		assertThat(response.getBody().path()).isEqualTo("/api/v1/students/99");
	}

	@Test
	void shouldReturnServiceUnavailableForDownstreamFailure() {
		when(request.getRequestURI()).thenReturn("/api/v1/integrations/name/aggregation");

		ResponseEntity<ApiErrorResponse> response = handler.handleDownstreamRequestFailure(
			new DownstreamRequestFailedException("Downstream failed", new RuntimeException("boom")),
			request
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(503);
		assertThat(response.getBody().message()).isEqualTo("Downstream failed");
		assertThat(response.getBody().path()).isEqualTo("/api/v1/integrations/name/aggregation");
	}
}
