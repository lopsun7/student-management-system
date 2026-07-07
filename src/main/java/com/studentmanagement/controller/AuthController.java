package com.studentmanagement.controller;

import com.studentmanagement.dto.AuthTokenRequest;
import com.studentmanagement.dto.AuthTokenResponse;
import com.studentmanagement.service.AuthTokenService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final AuthTokenService authTokenService;

	public AuthController(AuthenticationManager authenticationManager, AuthTokenService authTokenService) {
		this.authenticationManager = authenticationManager;
		this.authTokenService = authTokenService;
	}

	@PostMapping("/token")
	public AuthTokenResponse createToken(@Valid @RequestBody AuthTokenRequest request) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.username(), request.password())
		);
		return authTokenService.createToken(authentication);
	}
}
