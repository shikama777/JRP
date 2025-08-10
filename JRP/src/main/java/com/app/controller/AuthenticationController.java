package com.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.ResponseDto;

@RestController
@RequestMapping()
public class AuthenticationController {
	
	@Autowired
	private CsrfTokenRepository csrfTokenRepository;

	@GetMapping("/api/AD0000/authentication")
	public ResponseDto authenticationAD(HttpServletRequest request,
            HttpServletResponse response) {
		// 既存トークンを読み込み（Cookieから）
	    CsrfToken token = csrfTokenRepository.loadToken(request);

	    // 無ければ生成して保存（Set-Cookie: XSRF-TOKEN=...）
	    if (token == null) {
	    	CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
	        csrfTokenRepository.saveToken(csrfToken, request, response);
	    }

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		
		return responseDto;
	}
	
	@GetMapping("/api/ST0000/authentication")
	public ResponseDto authenticationST(HttpServletRequest request,
            HttpServletResponse response) {
		// 既存トークンを読み込み（Cookieから）
	    CsrfToken token = csrfTokenRepository.loadToken(request);

	    // 無ければ生成して保存（Set-Cookie: XSRF-TOKEN=...）
	    if (token == null) {
	    	CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
	        csrfTokenRepository.saveToken(csrfToken, request, response);
	    }

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		
		return responseDto;
	}
	
	@PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
	@GetMapping("/api/DEV/authentication")
	public ResponseDto authenticationDEV(HttpServletRequest request,
            HttpServletResponse response) {
		// 既存トークンを読み込み（Cookieから）
	    CsrfToken token = csrfTokenRepository.loadToken(request);

	    // 無ければ生成して保存（Set-Cookie: XSRF-TOKEN=...）
	    if (token == null) {
	    	CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
	        csrfTokenRepository.saveToken(csrfToken, request, response);
	    }

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		
		return responseDto;
	}

}
