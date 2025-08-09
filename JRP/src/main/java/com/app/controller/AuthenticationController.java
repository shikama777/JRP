package com.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.ResponseDto;

@RestController
@RequestMapping()
public class AuthenticationController {

	@GetMapping("/api/authentication")
	public ResponseDto authentication() {
		ResponseDto response = new ResponseDto();
		response.setSuccess(true);
		
		return response;
	}

}
