package com.studentmanagement.service;

import com.studentmanagement.dto.AuthTokenResponse;
import org.springframework.security.core.Authentication;

public interface AuthTokenService {

	AuthTokenResponse createToken(Authentication authentication);
}
