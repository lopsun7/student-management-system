package com.studentmanagement.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studentmanagement.model.Student;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class KafkaStudentEventPublisherTest {

	@Mock
	private KafkaTemplate<String, StudentEvent> kafkaTemplate;

	@Test
	void shouldPublishStudentCreatedEventToKafka() {
		KafkaStudentEventPublisher publisher = new KafkaStudentEventPublisher(kafkaTemplate, "student-events");
		Student student = new Student("Steven", "Zhao", "steven@example.com", "Kafka");
		student.setId(15L);
		RecordMetadata metadata = new RecordMetadata(new TopicPartition("student-events", 1), 0, 7, 0L, 0, 0);
		when(kafkaTemplate.send(eq("student-events"), eq("15"), org.mockito.ArgumentMatchers.any(StudentEvent.class)))
			.thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, metadata)));

		publisher.publishStudentCreated(student);

		ArgumentCaptor<StudentEvent> eventCaptor = ArgumentCaptor.forClass(StudentEvent.class);
		verify(kafkaTemplate).send(eq("student-events"), eq("15"), eventCaptor.capture());
		StudentEvent event = eventCaptor.getValue();
		assertThat(event.eventType()).isEqualTo("STUDENT_CREATED");
		assertThat(event.studentId()).isEqualTo(15L);
		assertThat(event.email()).isEqualTo("steven@example.com");
		assertThat(event.course()).isEqualTo("Kafka");
		assertThat(event.eventId()).isNotBlank();
		assertThat(event.occurredAt()).isNotNull();
	}
}
