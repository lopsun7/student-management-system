package com.studentmanagement.controller;

import com.studentmanagement.kafka.StudentEvent;
import com.studentmanagement.kafka.StudentEventConsumer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kafka")
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaValidationController {

	private final KafkaTemplate<String, StudentEvent> kafkaTemplate;
	private final StudentEventConsumer studentEventConsumer;
	private final String topicName;

	public KafkaValidationController(
			KafkaTemplate<String, StudentEvent> kafkaTemplate,
			StudentEventConsumer studentEventConsumer,
			@Value("${app.kafka.student-topic}") String topicName) {
		this.kafkaTemplate = kafkaTemplate;
		this.studentEventConsumer = studentEventConsumer;
		this.topicName = topicName;
	}

	@PostMapping("/student-events")
	public StudentEvent publishStudentEvent(@RequestBody StudentEvent request) {
		StudentEvent event = new StudentEvent(
			defaultString(request.eventId(), UUID.randomUUID().toString()),
			defaultString(request.eventType(), "STUDENT_CREATED"),
			request.studentId(),
			request.email(),
			request.course(),
			request.occurredAt() == null ? Instant.now() : request.occurredAt()
		);
		String key = event.studentId() == null ? event.eventId() : event.studentId().toString();
		kafkaTemplate.send(topicName, key, event);
		return event;
	}

	@GetMapping("/consumed-events")
	public List<StudentEvent> getConsumedEvents() {
		return studentEventConsumer.getConsumedEvents();
	}

	@DeleteMapping("/consumed-events")
	public void clearConsumedEvents() {
		studentEventConsumer.clearConsumedEvents();
	}

	private String defaultString(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
}
