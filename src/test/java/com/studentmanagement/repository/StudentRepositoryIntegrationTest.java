package com.studentmanagement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.studentmanagement.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StudentRepositoryIntegrationTest {

	@Autowired
	private StudentRepository studentRepository;

	@BeforeEach
	void setUp() {
		studentRepository.deleteAll();
	}

	@Test
	void shouldInsertFindSearchUpdateAndDeleteStudent() {
		Student saved = studentRepository.save(
			new Student("Ava", "Johnson", "ava@example.com", "Java Backend")
		);

		assertThat(saved.getId()).isNotNull();
		assertThat(studentRepository.findAll()).hasSize(1);
		assertThat(studentRepository.findById(saved.getId()))
			.isPresent()
			.get()
			.extracting(Student::getEmail)
			.isEqualTo("ava@example.com");
		assertThat(studentRepository.findByCourseContainingIgnoreCase("java"))
			.extracting(Student::getFirstName)
			.containsExactly("Ava");

		saved.setCourse("AWS");
		Student updated = studentRepository.save(saved);

		assertThat(updated.getCourse()).isEqualTo("AWS");
		assertThat(studentRepository.findByCourseContainingIgnoreCase("java")).isEmpty();

		studentRepository.delete(updated);

		assertThat(studentRepository.findById(saved.getId())).isEmpty();
		assertThat(studentRepository.findAll()).isEmpty();
	}
}
