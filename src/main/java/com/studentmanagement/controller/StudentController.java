package com.studentmanagement.controller;

import com.studentmanagement.model.Student;
import com.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping
	public List<Student> getAllStudents() {
		return studentService.getAllStudents();
	}

	@PostMapping
	public Student createStudent(@Valid @RequestBody Student student) {
		return studentService.createStudent(student);
	}

	@GetMapping("/{id}")
	public Student getStudentById(@PathVariable Long id) {
		return studentService.getStudentById(id);
	}

	@PutMapping("/{id}")
	public Student updateStudent(@PathVariable Long id, @Valid @RequestBody Student studentDetails) {
		return studentService.updateStudent(id, studentDetails);
	}

	@DeleteMapping("/{id}")
	public Map<String, Boolean> deleteStudent(@PathVariable Long id) {
		return studentService.deleteStudent(id);
	}
}
