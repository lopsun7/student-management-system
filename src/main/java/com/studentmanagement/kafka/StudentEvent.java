package com.studentmanagement.kafka;

import java.time.Instant;

public record StudentEvent(
		String eventId,
		String eventType,
		Long studentId,
		String email,
		String course,
		Instant occurredAt) {
}
