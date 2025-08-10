package com.app.security;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    	String redirectUri = "";

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
	        	
	        	OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
	        	
	        	// 既存の権限 + 新しいロールを追加
	            List<GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());
	            if (querySnapshot .getDocuments().get(0).get("role").equals("1")) {
                    // ユーザーが管理者の場合、ROLE_ADMINを追加
	            	authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	            	redirectUri = "AD";
	            } else if (querySnapshot .getDocuments().get(0).get("role").equals("2")) {
					// ユーザーが一般ユーザーの場合、ROLE_USERを追加
					authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
					redirectUri = "ST";
				} else if (querySnapshot .getDocuments().get(0).get("role").equals("0")) {
					authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
					authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
					redirectUri = "DEV";
	            }
	            
	            String jwt = jwtUtils.generateToken(username, authorities);
	        	
		        ResponseCookie cookie = ResponseCookie.from("x-auth-token", jwt)
		        	    .httpOnly(true)
		        	    .secure(true)
		        	    .sameSite("None")
		        	    .path("/")
		        	    .maxAge(Duration.ofDays(1))
		        	    .build();

	        	response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		            "An error occurred while querying Firestore");
		    return;
		}
		
        // 必要であればリダイレクト先を指定
        response.sendRedirect("http://localhost:5173/" + redirectUri);
    }
}