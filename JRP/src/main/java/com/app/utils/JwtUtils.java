package com.app.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
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
	
	public String generateToken(String userId, List<GrantedAuthority> authorities) {
		
		Instant now = Instant.now();
		
		List<String> roles = new ArrayList<>();
		for (GrantedAuthority authority : authorities) {
			if (authority.getAuthority().startsWith("ROLE_")) {
				roles.add(authority.getAuthority());
			}
        }
		
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("test")
				.claim("typ", "access")
				.claim("userId", userId)
				.claim("roles", roles) // ユーザーのロールを設定
				.issuedAt(now)
				.expiresAt(now.plus(1, ChronoUnit.DAYS))
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
