package com.studentmanagement.dto;

import java.time.Instant;

public record AuthTokenResponse(
	String accessToken,
	String tokenType,
	long expiresIn,
	Instant issuedAt,
	Instant expiresAt
) {
}
