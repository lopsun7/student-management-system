package com.studentmanagement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.studentmanagement.model.FailedDownstreamRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FailedDownstreamRequestRepositoryIntegrationTest {

	@Autowired
	private FailedDownstreamRequestRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("DELETE FROM failed_downstream_requests");
	}

	@Test
	void shouldSavePendingRequestAndMarkRecovered() {
		Long id = repository.savePending("Steven, Jessica", "connection timeout");

		FailedDownstreamRequest pending = repository.findById(id).orElseThrow();

		assertThat(pending.getAggregatedName()).isEqualTo("Steven, Jessica");
		assertThat(pending.getStatus()).isEqualTo("PENDING");
		assertThat(pending.getFailureReason()).isEqualTo("connection timeout");
		assertThat(pending.getAttemptCount()).isZero();
		assertThat(pending.getCreatedAt()).isNotNull();
		assertThat(pending.getUpdatedAt()).isNotNull();

		repository.markRecovered(id, "Steven, Jessica, Celine", 3);

		FailedDownstreamRequest recovered = repository.findById(id).orElseThrow();
		assertThat(recovered.getStatus()).isEqualTo("RECOVERED");
		assertThat(recovered.getRecoveredResponse()).isEqualTo("Steven, Jessica, Celine");
		assertThat(recovered.getAttemptCount()).isEqualTo(3);
	}

	@Test
	void shouldMarkRequestFailed() {
		Long id = repository.savePending("Steven", "first failure");

		repository.markFailed(id, "still unavailable", 3);

		FailedDownstreamRequest failed = repository.findById(id).orElseThrow();
		assertThat(failed.getStatus()).isEqualTo("FAILED");
		assertThat(failed.getFailureReason()).isEqualTo("still unavailable");
		assertThat(failed.getAttemptCount()).isEqualTo(3);
	}

	@Test
	void shouldReturnEmptyWhenRequestDoesNotExist() {
		assertThat(repository.findById(12345L)).isEmpty();
	}
}
