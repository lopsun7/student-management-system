package com.studentmanagement.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
	void shouldReturnNotFoundForMissingStudent() throws Exception {
		mockMvc.perform(get("/api/v1/students/{id}", 999L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Student not found with id: 999"))
			.andExpect(jsonPath("$.path").value("/api/v1/students/999"));
	}

	@Test
	void shouldExposeActuatorHealthEndpoint() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}
}
