package com.studentmanagement.controller;

import com.studentmanagement.dto.EmployeeRequest;
import com.studentmanagement.dto.EmployeeResponse;
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
@RequestMapping("/api/v1/employees")
public class EmployeeController {

	private final StudentService studentService;

	public EmployeeController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping
	public List<EmployeeResponse> getAllEmployees() {
		return studentService.getAllStudents().stream()
			.map(this::toEmployeeResponse)
			.toList();
	}

	@PostMapping
	public EmployeeResponse createEmployee(@Valid @RequestBody EmployeeRequest request) {
		return toEmployeeResponse(studentService.createStudent(toStudent(request)));
	}

	@GetMapping("/{id}")
	public EmployeeResponse getEmployeeById(@PathVariable Long id) {
		return toEmployeeResponse(studentService.getStudentById(id));
	}

	@PutMapping("/{id}")
	public EmployeeResponse updateEmployee(
			@PathVariable Long id,
			@Valid @RequestBody EmployeeRequest request) {
		return toEmployeeResponse(studentService.updateStudent(id, toStudent(request)));
	}

	@DeleteMapping("/{id}")
	public Map<String, Boolean> deleteEmployee(@PathVariable Long id) {
		return studentService.deleteStudent(id);
	}

	private Student toStudent(EmployeeRequest request) {
		return new Student(request.firstName(), request.lastName(), request.email(), request.department());
	}

	private EmployeeResponse toEmployeeResponse(Student student) {
		return new EmployeeResponse(
			student.getId(),
			student.getFirstName(),
			student.getLastName(),
			student.getEmail(),
			student.getCourse()
		);
	}
}
