package com.app.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ResponseDto {
	private boolean success;
	private String message;
	private Map<String, String> responseData;

}
