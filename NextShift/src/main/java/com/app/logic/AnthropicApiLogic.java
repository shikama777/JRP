package com.app.logic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class AnthropicApiLogic {
	@Value("${anthropics.api.key}")
	private String API_KEY;
	
	private final String MODEL = "claude-opus-4-20250514";
	
	public String callAnthropicApi(String prompt, String content) {
		
		Map<String, Object> body = new HashMap<>();
		body.put("model", MODEL);
		body.put("max_tokens", 1024);
		body.put("system", prompt);
		
		Map<String, String> message = new HashMap<>();
		message.put("role", "user");
		message.put("content", content);

		body.put("messages", List.of(message));
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonBody;
		
		String responseText = "";
		try {
			jsonBody = mapper.writeValueAsString(body);
        
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://api.anthropic.com/v1/messages"))
	                .header("Content-Type", "application/json")
	                .header("anthropic-version", "2023-06-01")
	                .header("x-api-key", API_KEY)
	                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
	                .build();
	        
	        HttpClient client = HttpClient.newHttpClient();
	        HttpResponse<String> response;
	      
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
			JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
			
			responseText = json.getAsJsonArray("content")
					.get(0).getAsJsonObject()
					.get("text").getAsString();

		} catch (IOException | InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
        
		return responseText;
		
	}

}
