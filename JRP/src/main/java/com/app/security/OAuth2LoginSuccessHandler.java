package com.app.security;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.app.utils.JwtUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;


@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    
    @Autowired
	private Firestore firestore;

    public OAuth2LoginSuccessHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

    	OAuth2User user = (OAuth2User) authentication.getPrincipal();
    	
        String username = user.getName();
        
        String email = user.getAttribute("email");
    
        ApiFuture<QuerySnapshot> userList = firestore.collection("users").whereEqualTo("gmail", email)
                .get();

		try {
			QuerySnapshot querySnapshot = userList.get();
			if (querySnapshot.isEmpty()) {
		        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
		                "No matching user found for the provided email");
		        return;
		    }
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		            "An error occurred while querying Firestore");
		    return;
		}
        
        String jwt = jwtUtils.generateToken(username);

        ResponseCookie cookie = ResponseCookie.from("x-auth-token", jwt)
        	    .httpOnly(true)
        	    .secure(true)
        	    .sameSite("None")
        	    .path("/")
        	    .maxAge(Duration.ofDays(1))
        	    .build();

        	response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 必要であればリダイレクト先を指定
        response.sendRedirect("http://localhost:5173/AD");
    }
}