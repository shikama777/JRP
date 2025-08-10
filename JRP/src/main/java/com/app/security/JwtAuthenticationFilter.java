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
	
	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
		String uri = request.getRequestURI();
		if (uri.startsWith("/login/oauth2/") || uri.startsWith("/oauth2/authorization")) {
	        filterChain.doFilter(request, response);
	        return;
	    }
		
		Cookie[] cookies = request.getCookies();
		
	    if (cookies != null) {
	    	boolean tokenFound = false;
	    	for(Cookie value: cookies) {
				if (value.getName().equals("x-auth-token")) {
					tokenFound = true;
					String xAuthToken = value.getValue();
					if (xAuthToken == null) {
		            	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
		                return;
		            }
		            // tokenの検証と認証
		            Jwt decodedJWT = jwtUtils.verifyToken(xAuthToken);
		            String userId = decodedJWT.getClaim("userId").toString();
		            
		            List<String> rolesList =  decodedJWT.getClaim("roles");
		            List<GrantedAuthority> roles = new ArrayList<>();
					for (String role : rolesList) {
						roles.add(new SimpleGrantedAuthority(role));
					}

		            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, roles));
		        
		            filterChain.doFilter(request, response);
				}
	    	}
	    	if (!tokenFound) { // If no token was found in the cookies
	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
	            return;
	    	}
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Missing or invalid Authorization header");
			return;
		}
	}
}
