package com.studentmanagement.kafka;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class StudentEventConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(StudentEventConsumer.class);

	private final List<StudentEvent> consumedEvents = new CopyOnWriteArrayList<>();

	@KafkaListener(
		topics = "${app.kafka.student-topic}",
		groupId = "${app.kafka.consumer-group}",
		concurrency = "${spring.kafka.listener.concurrency:3}"
	)
	public void consume(
			StudentEvent event,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
			@Header(KafkaHeaders.OFFSET) long offset) {
		consumedEvents.add(event);
		LOGGER.info(
			"Consumed Kafka student event {} from partition {} offset {} on thread {}",
			event.eventId(),
			partition,
			offset,
			Thread.currentThread().getName()
		);
	}

	public List<StudentEvent> getConsumedEvents() {
		return List.copyOf(consumedEvents);
	}

	public void clearConsumedEvents() {
		consumedEvents.clear();
	}
}
