package com.app.security;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
	private Firestore firestore;
    
    @Value("${frontend.url}")
	private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
    	
    	System.out.println("OAuth2LoginSuccessHandler invoked");

    	OAuth2User user = (OAuth2User) authentication.getPrincipal();
        
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
	        	
	        	String userId = querySnapshot .getDocuments().get(0).getId();
	            
	            String jwt = jwtUtils.generateRefreshToken(userId);
	        	
		        ResponseCookie cookie = ResponseCookie.from("__session", jwt)
		        	    .httpOnly(true)
		        	    .secure(true)
		        	    .sameSite("None")
		        	    .path("/")
		        	    .maxAge(Duration.ofDays(7))
		        	    .build();

	        	response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		            "An error occurred while querying Firestore");
		    return;
		}
		
        // 必要であればリダイレクト先を指定
        response.sendRedirect(frontendUrl + "/auth/callback");
    }
}