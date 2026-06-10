package com.studentmanagement.aspect;

import static org.assertj.core.api.Assertions.assertThat;

import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ServiceLoggingAspectTest {

	@Autowired
	private StudentService studentService;

	@Autowired
	private StudentRepository studentRepository;

	@BeforeEach
	void setUp() {
		studentRepository.deleteAll();
	}

	@Test
	void shouldInterceptServiceMethodButNotInternalHelperMethods(CapturedOutput output) {
		Student student = new Student(" Ava ", " Johnson ", " AVA.JOHNSON@EXAMPLE.COM ", " Computer Science ");

		studentService.createStudent(student);

		assertThat(output).contains("AOP BEFORE -> StudentServiceImpl.createStudent(..)");
		assertThat(output).contains("AOP AFTER -> StudentServiceImpl.createStudent(..)");
		assertThat(output).contains("AOP AROUND START -> StudentServiceImpl.createStudent(..)");
		assertThat(output).contains("AOP AROUND END -> StudentServiceImpl.createStudent(..)");

		assertThat(output).contains("DIRECT CALL -> StudentServiceImpl.prepareStudentForSave(..)");
		assertThat(output).contains("DIRECT CALL -> StudentServiceImpl.normalizeStudentData(..)");

		assertThat(output).doesNotContain("AOP BEFORE -> StudentServiceImpl.prepareStudentForSave(..)");
		assertThat(output).doesNotContain("AOP BEFORE -> StudentServiceImpl.normalizeStudentData(..)");
	}
}
