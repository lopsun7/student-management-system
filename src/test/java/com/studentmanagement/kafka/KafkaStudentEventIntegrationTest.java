package com.studentmanagement.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.studentmanagement.model.Student;
import com.studentmanagement.service.StudentEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(properties = {
	"app.kafka.enabled=true",
	"app.kafka.student-topic=student-events-integration-test",
	"app.kafka.student-topic-partitions=3",
	"app.kafka.student-topic-replicas=3",
	"app.kafka.consumer-group=student-management-events-integration-test",
	"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
	"spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
	"spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
	"spring.kafka.consumer.auto-offset-reset=earliest",
	"spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
	"spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
	"spring.kafka.consumer.properties.spring.json.trusted.packages=com.studentmanagement.kafka",
	"spring.kafka.listener.concurrency=3"
})
@EmbeddedKafka(
	count = 3,
	partitions = 3,
	topics = "student-events-integration-test",
	brokerProperties = {
		"auto.create.topics.enable=true",
		"offsets.topic.replication.factor=3",
		"transaction.state.log.replication.factor=3",
		"transaction.state.log.min.isr=2"
	}
)
class KafkaStudentEventIntegrationTest {

	@Autowired
	private StudentEventPublisher studentEventPublisher;

	@Autowired
	private StudentEventConsumer studentEventConsumer;

	@BeforeEach
	void setUp() {
		studentEventConsumer.clearConsumedEvents();
	}

	@Test
	void shouldProduceAndConsumeStudentEvent() throws InterruptedException {
		Student student = new Student("Steven", "Zhao", "steven@example.com", "Kafka");
		student.setId(88L);

		studentEventPublisher.publishStudentCreated(student);

		assertThat(waitForConsumedEvent()).isTrue();
		assertThat(studentEventConsumer.getConsumedEvents())
			.anySatisfy(event -> {
				assertThat(event.eventType()).isEqualTo("STUDENT_CREATED");
				assertThat(event.studentId()).isEqualTo(88L);
				assertThat(event.email()).isEqualTo("steven@example.com");
				assertThat(event.course()).isEqualTo("Kafka");
			});
	}

	private boolean waitForConsumedEvent() throws InterruptedException {
		for (int attempt = 0; attempt < 30; attempt++) {
			if (!studentEventConsumer.getConsumedEvents().isEmpty()) {
				return true;
			}
			Thread.sleep(250);
		}
		return false;
	}
}
