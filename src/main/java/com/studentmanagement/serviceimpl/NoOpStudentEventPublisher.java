package com.studentmanagement.serviceimpl;

import com.studentmanagement.model.Student;
import com.studentmanagement.service.StudentEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpStudentEventPublisher implements StudentEventPublisher {

	@Override
	public void publishStudentCreated(Student student) {
		// Kafka is disabled by default, so local CRUD demos do not require a broker.
	}
}
