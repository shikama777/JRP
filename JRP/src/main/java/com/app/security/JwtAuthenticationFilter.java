package com.app.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.utils.JwtUtils;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	@Autowired
    private JwtUtils jwtUtils;
	
	 @Override
	 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	         throws ServletException, IOException {

	     String uri = request.getRequestURI();
	     if (uri.startsWith("/login/oauth2/") || uri.startsWith("/oauth2/authorization") || uri.equals("/api/refresh")) {
	         filterChain.doFilter(request, response);
	         return;
	     }

	  // AuthorizationヘッダーからBearerトークンを取得
	     String authHeader = request.getHeader("Authorization");
	     if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
	         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
	         return;
	     }

	     String token = authHeader.substring(7).trim(); // "Bearer " 以降
	     if (token.isEmpty()) {
	         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
	         return;
	     }

	     try {
	    	// トークン検証（署名・exp 等）
	        Jwt decodedJWT = jwtUtils.verifyToken(token);

	        // 必要情報の取り出し
	        String userId = decodedJWT.getClaimAsString("userId");

	        List<String> rolesList = decodedJWT.getClaimAsStringList("roles");
	        if (rolesList == null) rolesList = java.util.Collections.emptyList();

	        List<GrantedAuthority> authorities = new ArrayList<>();
	        for (String role : rolesList) {
	            authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
	        }

	        // セキュリティコンテキストに反映
	        UsernamePasswordAuthenticationToken authentication =
	                new UsernamePasswordAuthenticationToken(userId, null, authorities);
	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        filterChain.doFilter(request, response);
	    } catch (Exception e) {
	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
	    }
	 }
}
