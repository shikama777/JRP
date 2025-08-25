package com.app.security;

import java.net.URLDecoder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

public class HttpCookieOAuth2AuthorizationRequestRepository
implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String COOKIE_NAME = "__session";
	private static final int COOKIE_EXPIRE_SECONDS = 180;
	private static final String KEY_REQ = "oauth2_request";
	private static final String KEY_STATE = "oauth2_state";
	
	private static java.util.Map<String,String> parse(String raw) {
		Map<String,String> m = new LinkedHashMap<>();

		if (raw == null || raw.isEmpty()) {
			return m;
		}

		for (String p : raw.split("&")) {
			int i = p.indexOf('=');
			if (i > 0) {
				String k = URLDecoder.decode(p.substring(0,i), java.nio.charset.StandardCharsets.UTF_8);
		        String v = URLDecoder.decode(p.substring(i+1), java.nio.charset.StandardCharsets.UTF_8);
		        m.put(k, v);
		    }
		}
		return m;
	}
	
	private static String build(java.util.Map<String,String> m) {
		return m.entrySet().stream()
		    .map(e -> java.net.URLEncoder.encode(e.getKey(), java.nio.charset.StandardCharsets.UTF_8)
		           + "=" + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
		    .collect(java.util.stream.Collectors.joining("&"));
	}
	
	private static String readCookie(HttpServletRequest req) {
		Cookie[] cs = req.getCookies();
		
		if (cs == null) {
			return null;
		}

		for (jakarta.servlet.http.Cookie c : cs) {
		    if (COOKIE_NAME.equals(c.getName())) {
		    	return c.getValue();
		    }
		}
		return null;
	}

	private static void writeCookie(HttpServletResponse res, String value, int maxAge) {
		String header = COOKIE_NAME + "=" + (value == null ? "" : value)
		        + "; Path=/"
		        + "; HttpOnly; Secure; SameSite=None"
		        + "; Max-Age=" + maxAge;
			res.addHeader("Set-Cookie", header);
	}
		
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		String raw = readCookie(request);

		if (raw == null) {
			return null;
		}

		var map = parse(raw);
		
		String data = map.get(KEY_REQ);
		String stateInCookie = map.get(KEY_STATE);
		String stateInParam  = request.getParameter("state");

		if (data == null || stateInCookie == null || !stateInCookie.equals(stateInParam)) {
			return null;
		}
		
		byte[] bytes = Base64.getUrlDecoder().decode(data); 
		
		Object obj = SerializationUtils.deserialize(bytes);

		return (obj instanceof OAuth2AuthorizationRequest) ? (OAuth2AuthorizationRequest) obj : null;
	}
	
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
	                                 HttpServletRequest request,
	                                 HttpServletResponse response) {
		if (authorizationRequest == null) {
		    writeCookie(response, "", 0); // 全消し
		    return;
		}

		byte[] bytes = SerializationUtils.serialize(authorizationRequest);
		
		String encoded = Base64.getUrlEncoder().encodeToString(bytes);
		
		var map = new LinkedHashMap<String,String>();
		
		map.put(KEY_REQ, encoded);
		
		map.put(KEY_STATE, authorizationRequest.getState());
		writeCookie(response, build(map), COOKIE_EXPIRE_SECONDS);
	}
	
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
	                                                         HttpServletResponse response) {
		OAuth2AuthorizationRequest reqObj = loadAuthorizationRequest(request);
		writeCookie(response, "", 0); // 後始末
		return reqObj;
	}
}