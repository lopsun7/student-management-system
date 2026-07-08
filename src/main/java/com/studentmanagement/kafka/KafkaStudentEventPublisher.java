package com.studentmanagement.kafka;

import com.studentmanagement.model.Student;
import com.studentmanagement.service.StudentEventPublisher;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaStudentEventPublisher implements StudentEventPublisher {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStudentEventPublisher.class);

	private final KafkaTemplate<String, StudentEvent> kafkaTemplate;
	private final String topicName;

	public KafkaStudentEventPublisher(
			KafkaTemplate<String, StudentEvent> kafkaTemplate,
			@Value("${app.kafka.student-topic}") String topicName) {
		this.kafkaTemplate = kafkaTemplate;
		this.topicName = topicName;
	}

	@Override
	public void publishStudentCreated(Student student) {
		StudentEvent event = new StudentEvent(
			UUID.randomUUID().toString(),
			"STUDENT_CREATED",
			student.getId(),
			student.getEmail(),
			student.getCourse(),
			Instant.now()
		);
		String key = student.getId() == null ? event.eventId() : student.getId().toString();
		kafkaTemplate.send(topicName, key, event)
			.whenComplete((result, ex) -> {
				if (ex != null) {
					LOGGER.error("Failed to publish Kafka student event {}", event.eventId(), ex);
					return;
				}
				LOGGER.info(
					"Published Kafka student event {} to topic {} partition {} offset {}",
					event.eventId(),
					result.getRecordMetadata().topic(),
					result.getRecordMetadata().partition(),
					result.getRecordMetadata().offset()
				);
			});
	}
}
