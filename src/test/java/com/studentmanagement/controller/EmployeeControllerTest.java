package com.studentmanagement.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentmanagement.dto.EmployeeRequest;
import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "steven", roles = "USER")
class EmployeeControllerTest {

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
	void shouldCreateEmployee() throws Exception {
		EmployeeRequest request = new EmployeeRequest("Steven", "Zhao", "steven.employee@example.com", "Engineering");

		mockMvc.perform(post("/api/v1/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.firstName").value("Steven"))
			.andExpect(jsonPath("$.department").value("Engineering"));
	}

	@Test
	void shouldReturnAndUpdateEmployees() throws Exception {
		Student savedEmployee = studentRepository.save(
			new Student("Ava", "Jones", "ava.employee@example.com", "HR")
		);

		mockMvc.perform(get("/api/v1/employees"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].department").value("HR"));

		EmployeeRequest update = new EmployeeRequest("Ava", "Jones", "ava.updated@example.com", "Finance");

		mockMvc.perform(put("/api/v1/employees/{id}", savedEmployee.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(update)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(savedEmployee.getId()))
			.andExpect(jsonPath("$.department").value("Finance"));
	}

	@Test
	void shouldDeleteEmployee() throws Exception {
		Student savedEmployee = studentRepository.save(
			new Student("Leo", "Kim", "leo.employee@example.com", "Engineering")
		);

		mockMvc.perform(delete("/api/v1/employees/{id}", savedEmployee.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.deleted").value(true));
	}
}
