package com.app.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
	
	 private void allowCors(HttpServletRequest req, HttpServletResponse res) { String origin = req.getHeader("Origin"); if ("https://next-shift-13fc0.web.app".equals(origin)) { res.setHeader("Access-Control-Allow-Origin", origin); res.setHeader("Vary", "Origin"); res.setHeader("Access-Control-Allow-Credentials", "true"); res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-XSRF-TOKEN, Authorization, X-Requested-With"); res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS"); } }

	 @Override
	 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	         throws ServletException, IOException {

	     String uri = request.getRequestURI();
	     if (uri.startsWith("/login/oauth2/") || uri.startsWith("/oauth2/authorization")) {
	         filterChain.doFilter(request, response);
	         return;
	     }

	     Cookie[] cookies = request.getCookies();
	     if (cookies == null) {
	         allowCors(request, response);
	         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
	         return;
	     }

	     boolean tokenFound = false;
	     for (Cookie value : cookies) {
	         if ("x-auth-token".equals(value.getName())) {
	             tokenFound = true;
	             String xAuthToken = value.getValue();
	             if (xAuthToken == null) {
	                 allowCors(request, response);
	                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
	                 return;
	             }
	             try {
	                 Jwt decodedJWT = jwtUtils.verifyToken(xAuthToken);
	                 System.out.println("JWT claims: " + decodedJWT.getClaims());
	                 String userId = decodedJWT.getClaimAsString("userId");

	                 List<String> rolesList = decodedJWT.getClaimAsStringList("roles");
	                 if (rolesList == null) rolesList = java.util.Collections.emptyList();

	                 List<GrantedAuthority> roles = new ArrayList<>();
	                 for (String role : rolesList) {
	                     roles.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
	                 }

	                 SecurityContextHolder.getContext()
	                         .setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, roles));
	                 filterChain.doFilter(request, response);
	                 return;

	             } catch (Exception e) {
	                 allowCors(request, response);
	                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
	                 return;
	             }
	         }
	     }

	     if (!tokenFound) {
	         allowCors(request, response);
	         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
	     }
	 }
}
