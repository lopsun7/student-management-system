package com.studentmanagement.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

class KafkaTopicConfigTest {

	private final KafkaTopicConfig kafkaTopicConfig = new KafkaTopicConfig();

	@Test
	void shouldCreateStudentTopicWithConfiguredPartitionsAndReplicas() {
		NewTopic topic = kafkaTopicConfig.studentEventsTopic("student-events", 3, 3);

		assertThat(topic.name()).isEqualTo("student-events");
		assertThat(topic.numPartitions()).isEqualTo(3);
		assertThat(topic.replicationFactor()).isEqualTo((short) 3);
	}
}
