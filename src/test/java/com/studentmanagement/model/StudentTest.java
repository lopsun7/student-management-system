package com.studentmanagement.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StudentTest {

	@Test
	void shouldCreateStudentWithConstructorAndAllowUpdates() {
		Student student = new Student("Ava", "Johnson", "ava@example.com", "Java");

		student.setId(1L);
		student.setFirstName("Steven");
		student.setLastName("Zhao");
		student.setEmail("steven@example.com");
		student.setCourse("AWS");

		assertThat(student.getId()).isEqualTo(1L);
		assertThat(student.getFirstName()).isEqualTo("Steven");
		assertThat(student.getLastName()).isEqualTo("Zhao");
		assertThat(student.getEmail()).isEqualTo("steven@example.com");
		assertThat(student.getCourse()).isEqualTo("AWS");
	}
}
