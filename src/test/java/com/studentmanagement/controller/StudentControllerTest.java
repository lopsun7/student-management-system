package com.studentmanagement.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StudentRepository studentRepository;

	@BeforeEach
	void setUp() {
		studentRepository.deleteAll();
	}

	@Test
	void shouldCreateStudent() throws Exception {
		Student student = new Student("Ava", "Johnson", "ava.johnson@example.com", "Computer Science");

		mockMvc.perform(post("/api/v1/students")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(student)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.firstName").value("Ava"))
			.andExpect(jsonPath("$.course").value("Computer Science"));
	}

	@Test
	void shouldReturnAllStudents() throws Exception {
		studentRepository.save(new Student("Ava", "Johnson", "ava@example.com", "Java"));
		studentRepository.save(new Student("Leo", "Brown", "leo@example.com", "AWS"));

		mockMvc.perform(get("/api/v1/students"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].firstName").value("Ava"))
			.andExpect(jsonPath("$[1].firstName").value("Leo"));
	}

	@Test
	void shouldReturnStudentById() throws Exception {
		Student savedStudent = studentRepository.save(
			new Student("Noah", "Williams", "noah.williams@example.com", "Mathematics")
		);

		mockMvc.perform(get("/api/v1/students/{id}", savedStudent.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("noah.williams@example.com"))
			.andExpect(jsonPath("$.lastName").value("Williams"));
	}

	@Test
	void shouldSearchStudentsByCourse() throws Exception {
		studentRepository.save(new Student("Ava", "Johnson", "ava@example.com", "Java Backend"));
		studentRepository.save(new Student("Leo", "Brown", "leo@example.com", "Python"));

		mockMvc.perform(get("/api/v1/students/search").param("course", "java"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].firstName").value("Ava"))
			.andExpect(jsonPath("$[0].course").value("Java Backend"));
	}

	@Test
	void shouldUpdateStudent() throws Exception {
		Student savedStudent = studentRepository.save(
			new Student("Noah", "Williams", "noah.williams@example.com", "Mathematics")
		);
		Student updateRequest = new Student("Noah", "Smith", "NOAH.SMITH@EXAMPLE.COM", "AWS");

		mockMvc.perform(put("/api/v1/students/{id}", savedStudent.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(savedStudent.getId()))
			.andExpect(jsonPath("$.lastName").value("Smith"))
			.andExpect(jsonPath("$.email").value("noah.smith@example.com"))
			.andExpect(jsonPath("$.course").value("AWS"));
	}

	@Test
	void shouldDeleteStudent() throws Exception {
		Student savedStudent = studentRepository.save(
			new Student("Noah", "Williams", "noah.williams@example.com", "Mathematics")
		);

		mockMvc.perform(delete("/api/v1/students/{id}", savedStudent.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.deleted").value(true));
	}

	@Test
	void shouldReturnNotFoundForMissingStudent() throws Exception {
		mockMvc.perform(get("/api/v1/students/{id}", 999L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Student not found with id: 999"))
			.andExpect(jsonPath("$.path").value("/api/v1/students/999"));
	}

	@Test
	void shouldReturnValidationErrorsForInvalidStudent() throws Exception {
		Student invalidStudent = new Student("", "Johnson", "not-an-email", "");

		mockMvc.perform(post("/api/v1/students")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidStudent)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.validationErrors.firstName").value("First name is required"))
			.andExpect(jsonPath("$.validationErrors.email").value("Email must be valid"))
			.andExpect(jsonPath("$.validationErrors.course").value("Course is required"));
	}

	@Test
	void shouldExposeActuatorHealthEndpoint() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}
}
