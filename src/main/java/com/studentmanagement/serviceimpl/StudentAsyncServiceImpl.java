package com.studentmanagement.serviceimpl;

import com.studentmanagement.service.StudentAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class StudentAsyncServiceImpl implements StudentAsyncService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StudentAsyncServiceImpl.class);

	@Override
	@Async("studentTaskExecutor")
	public void logStudentCreated(Long studentId, String email) {
		LOGGER.info("ASYNC -> Student created with id: {} and email: {}", studentId, email);
	}

}
