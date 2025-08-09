package com.app.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

	@Autowired
	private JwtEncoder jwtEncoder;
	
	@Autowired
	private JwtDecoder jwtDecoder;
	
	public String generateToken(String username) {
		
		Instant now = Instant.now();
		
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("test")
				.claim("username", username)
				.claim("role", "ROLE_USER") // ユーザーのロールを設定
				.issuedAt(now)
				.expiresAt(now.plus(30, ChronoUnit.MINUTES))
				.build();
		
		JwsHeader header = JwsHeader.with(() -> "HS256") // アルゴリズム
	            .build();
		
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
	
	public Jwt verifyToken(String token) {
		try {
			return jwtDecoder.decode(token);
		} catch (Exception e) {
			throw new RuntimeException("Invalid JWT token", e);
		}
	}

}
