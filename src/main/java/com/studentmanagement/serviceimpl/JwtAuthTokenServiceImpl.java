package com.studentmanagement.serviceimpl;

import com.studentmanagement.dto.AuthTokenResponse;
import com.studentmanagement.service.AuthTokenService;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthTokenServiceImpl implements AuthTokenService {

	private final JwtEncoder jwtEncoder;
	private final Duration tokenTtl;

	public JwtAuthTokenServiceImpl(
			JwtEncoder jwtEncoder,
			@Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
		this.jwtEncoder = jwtEncoder;
		this.tokenTtl = Duration.ofMinutes(expirationMinutes);
	}

	@Override
	public AuthTokenResponse createToken(Authentication authentication) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(tokenTtl);
		String scope = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(" "));

		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("student-management-system")
			.subject(authentication.getName())
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.claim("scope", scope)
			.build();

		String token = jwtEncoder.encode(
			JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
		).getTokenValue();

		return new AuthTokenResponse(token, "Bearer", tokenTtl.toSeconds(), issuedAt, expiresAt);
	}
}
