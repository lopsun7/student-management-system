package com.studentmanagement.service;

import com.studentmanagement.model.Student;

public interface StudentEventPublisher {

	void publishStudentCreated(Student student);
}
