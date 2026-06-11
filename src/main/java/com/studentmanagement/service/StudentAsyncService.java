package com.studentmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class StudentAsyncService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StudentAsyncService.class);

	@Async
	public void logStudentCreated(Long studentId, String email) {
		LOGGER.info("ASYNC -> Student created with id: {} and email: {}", studentId, email);
	}
}
