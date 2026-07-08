package com.studentmanagement.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.StudentAsyncService;
import com.studentmanagement.service.StudentEventPublisher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private StudentAsyncService studentAsyncService;

	@Mock
	private StudentEventPublisher studentEventPublisher;

	@InjectMocks
	private StudentServiceImpl studentService;

	@Test
	void shouldReturnAllStudents() {
		List<Student> students = List.of(new Student("Ava", "Jones", "ava@example.com", "Java"));
		when(studentRepository.findAll()).thenReturn(students);

		assertThat(studentService.getAllStudents()).isSameAs(students);
	}

	@Test
	void shouldSearchStudentsByCourse() {
		List<Student> students = List.of(new Student("Leo", "Kim", "leo@example.com", "AWS"));
		when(studentRepository.findByCourseContainingIgnoreCase("aws")).thenReturn(students);

		assertThat(studentService.searchStudentsByCourse("aws")).isSameAs(students);
	}

	@Test
	void shouldNormalizeAndCreateStudentThenLogAsync() {
		Student student = new Student(" Steven ", " Zhao ", " STEVEN@EXAMPLE.COM ", " Java ");
		when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
			Student saved = invocation.getArgument(0);
			saved.setId(10L);
			return saved;
		});

		Student savedStudent = studentService.createStudent(student);

		assertThat(savedStudent.getFirstName()).isEqualTo("Steven");
		assertThat(savedStudent.getLastName()).isEqualTo("Zhao");
		assertThat(savedStudent.getEmail()).isEqualTo("steven@example.com");
		assertThat(savedStudent.getCourse()).isEqualTo("Java");
		verify(studentAsyncService).logStudentCreated(10L, "steven@example.com");
		verify(studentEventPublisher).publishStudentCreated(savedStudent);
	}

	@Test
	void shouldReturnStudentById() {
		Student student = new Student("Ava", "Jones", "ava@example.com", "Java");
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

		assertThat(studentService.getStudentById(1L)).isSameAs(student);
	}

	@Test
	void shouldThrowWhenStudentDoesNotExist() {
		when(studentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> studentService.getStudentById(99L))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Student not found with id: 99");
	}

	@Test
	void shouldUpdateExistingStudent() {
		Student existing = new Student("Old", "Name", "old@example.com", "Old Course");
		existing.setId(7L);
		when(studentRepository.findById(7L)).thenReturn(Optional.of(existing));
		when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Student updated = studentService.updateStudent(
			7L,
			new Student(" New ", " User ", " NEW@EXAMPLE.COM ", " Spring ")
		);

		assertThat(updated.getFirstName()).isEqualTo("New");
		assertThat(updated.getLastName()).isEqualTo("User");
		assertThat(updated.getEmail()).isEqualTo("new@example.com");
		assertThat(updated.getCourse()).isEqualTo("Spring");
		ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
		verify(studentRepository).save(captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(7L);
	}

	@Test
	void shouldDeleteExistingStudent() {
		Student student = new Student("Ava", "Jones", "ava@example.com", "Java");
		student.setId(5L);
		when(studentRepository.findById(5L)).thenReturn(Optional.of(student));

		Map<String, Boolean> response = studentService.deleteStudent(5L);

		assertThat(response).containsEntry("deleted", true);
		verify(studentRepository).delete(student);
	}
}
