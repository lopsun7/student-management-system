package com.studentmanagement.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class StudentEventConsumerTest {

	private final StudentEventConsumer studentEventConsumer = new StudentEventConsumer();

	@Test
	void shouldStoreConsumedEventsForValidation() {
		StudentEvent event = new StudentEvent(
			"event-1",
			"STUDENT_CREATED",
			1L,
			"steven@example.com",
			"Kafka",
			Instant.parse("2026-07-08T12:00:00Z")
		);

		studentEventConsumer.consume(event, 0, 12L);

		assertThat(studentEventConsumer.getConsumedEvents()).containsExactly(event);
	}

	@Test
	void shouldClearConsumedEvents() {
		studentEventConsumer.consume(
			new StudentEvent("event-2", "STUDENT_CREATED", 2L, "ava@example.com", "AWS", Instant.now()),
			1,
			20L
		);

		studentEventConsumer.clearConsumedEvents();

		assertThat(studentEventConsumer.getConsumedEvents()).isEmpty();
	}
}
