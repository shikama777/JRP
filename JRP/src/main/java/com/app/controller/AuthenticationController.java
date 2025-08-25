package com.app.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.ResponseDto;
import com.app.utils.JwtUtils;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

@RestController
@RequestMapping()
public class AuthenticationController {
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private Firestore firestore;
	
	@Value("${frontend.url}")
	private String frontendUrl;
	
	@GetMapping("/api/refresh")
    public ResponseDto refresh(@CookieValue(name="__session", required=false) String refreshToken,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		System.out.println("Refresh token from cookie: " + refreshToken);
		
		String redirectUri = "";
		
		ResponseDto responseDto = new ResponseDto();

		try {
			
			Jwt decodedJWT = jwtUtils.verifyToken(refreshToken);
			
			String userId = decodedJWT.getClaimAsString("userId");
			
			DocumentSnapshot user = firestore.collection("users").document(userId).get().get();

			// ユーザーが存在しない場合、401エラーを返す
			if (user == null || !user.exists()) {
				responseDto.setSuccess(false);     
		    }
			
			// ユーザーIDがトークンと一致しない場合、401エラーを返す
			if (decodedJWT.getClaimAsString("userId") == null || !decodedJWT.getClaimAsString("userId").equals(userId)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
		                "Invalid token: user ID does not match");   
			}

	    	// 既存の権限 + 新しいロールを追加
	        List<GrantedAuthority> authorities = new ArrayList<>();
	        if (user.get("role").equals("1")) {
	            // ユーザーが管理者の場合、ROLE_ADMINを追加
	        	authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	        	redirectUri = "/AD";
	        } else if (user.get("role").equals("2")) {
				// ユーザーが一般ユーザーの場合、ROLE_USERを追加
				authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				redirectUri = "/ST";
			} else if (user.get("role").equals("0")) {
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
				authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				redirectUri = "/DEV";
	        }
	        
	        String token = jwtUtils.generateAuthenticationToken(userId, authorities);
	        
	        responseDto.setSuccess(true);
	        
	        responseDto.setResponseData(Map.of("x-auth-token", token, "redirect-uri", redirectUri));
		} catch (Exception e) {
			responseDto.setSuccess(false);
		}

        return responseDto;
    }

	@GetMapping("/api/AD/authentication")
	public ResponseDto authenticationAD(HttpServletRequest request,
            HttpServletResponse response) {

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		
		return responseDto;
	}
	
	@GetMapping("/api/ST/authentication")
	public ResponseDto authenticationST(HttpServletRequest request,
            HttpServletResponse response) {

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		
		return responseDto;
	}
	
	@PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
	@GetMapping("/api/DEV/authentication")
	public ResponseDto authenticationDEV(HttpServletRequest request,
            HttpServletResponse response) {
		// 既存トークンを読み込み（Cookieから）

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		
		return responseDto;
	}

}
