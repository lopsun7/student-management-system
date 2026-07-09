package com.studentmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
	@NotBlank(message = "First name is required")
	@Size(max = 50, message = "First name must be at most 50 characters")
	String firstName,
	@NotBlank(message = "Last name is required")
	@Size(max = 50, message = "Last name must be at most 50 characters")
	String lastName,
	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	String email,
	@NotBlank(message = "Department is required")
	@Size(max = 100, message = "Department must be at most 100 characters")
	String department) {
}
