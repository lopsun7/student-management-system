package com.studentmanagement.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FailedDownstreamRequestTest {

	@Test
	void shouldStoreFailedRequestFields() {
		LocalDateTime createdAt = LocalDateTime.now().minusMinutes(1);
		LocalDateTime updatedAt = LocalDateTime.now();
		FailedDownstreamRequest request = new FailedDownstreamRequest();

		request.setId(1L);
		request.setAggregatedName("Steven, Jessica");
		request.setStatus("PENDING");
		request.setFailureReason("timeout");
		request.setAttemptCount(2);
		request.setRecoveredResponse("Steven, Jessica, Celine");
		request.setCreatedAt(createdAt);
		request.setUpdatedAt(updatedAt);

		assertThat(request.getId()).isEqualTo(1L);
		assertThat(request.getAggregatedName()).isEqualTo("Steven, Jessica");
		assertThat(request.getStatus()).isEqualTo("PENDING");
		assertThat(request.getFailureReason()).isEqualTo("timeout");
		assertThat(request.getAttemptCount()).isEqualTo(2);
		assertThat(request.getRecoveredResponse()).isEqualTo("Steven, Jessica, Celine");
		assertThat(request.getCreatedAt()).isEqualTo(createdAt);
		assertThat(request.getUpdatedAt()).isEqualTo(updatedAt);
	}
}
