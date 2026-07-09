package com.studentmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GmailLoginRequest(
	@NotBlank(message = "Gmail address is required")
	@Email(message = "Gmail address must be valid")
	String email) {
}
