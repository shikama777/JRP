package com.app.security;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class HttpCookieOAuth2AuthorizationRequestRepository
implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String COOKIE_NAME = "__session";        // ★ Firebase Hosting で唯一通る
	private static final String COOKIE_DOMAIN = "next-shift-13fc0.web.app";
	private static final int COOKIE_EXPIRE_SECONDS = 600;         // 10分
	private static final String KEY_REQ = "oauth2_req";           // 旧 OAUTH2_AUTHZ_REQ
	private static final String KEY_STATE = "oauth2_state";       // 旧 OAUTH2_AUTHZ_STATE
	
	// k=v&k=v の簡易パーサ
	private static java.util.Map<String,String> parse(String raw) {
		Map<String,String> m = new LinkedHashMap<>();
		if (raw == null || raw.isEmpty()) return m;
		for (String p : raw.split("&")) {
			int i = p.indexOf('=');
			if (i > 0) {
				String k = java.net.URLDecoder.decode(p.substring(0,i), java.nio.charset.StandardCharsets.UTF_8);
		        String v = java.net.URLDecoder.decode(p.substring(i+1), java.nio.charset.StandardCharsets.UTF_8);
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
		jakarta.servlet.http.Cookie[] cs = req.getCookies();
		if (cs == null) return null;
		for (jakarta.servlet.http.Cookie c : cs) {
		    if (COOKIE_NAME.equals(c.getName())) return c.getValue();
		}
		return null;
	}

	private static void writeCookie(HttpServletResponse res, String value, int maxAge) {
		String header = COOKIE_NAME + "=" + (value == null ? "" : value)
		        + "; Path=/"
		        + "; Domain=" + COOKIE_DOMAIN
		        + "; HttpOnly; Secure; SameSite=None"
		        + "; Max-Age=" + maxAge;
			res.addHeader("Set-Cookie", header);
			res.setHeader("Cache-Control", "private"); // ★ 念のため
		}
		
		@Override
		public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		String raw = readCookie(request);
		if (raw == null) return null;
		var map = parse(raw);
		String data = map.get(KEY_REQ);
		String stateInCookie = map.get(KEY_STATE);
		String stateInParam  = request.getParameter("state");
		if (data == null || stateInCookie == null || !stateInCookie.equals(stateInParam)) return null;
		
		byte[] bytes;
		try { bytes = java.util.Base64.getUrlDecoder().decode(data); }
		catch (IllegalArgumentException e) { bytes = java.util.Base64.getDecoder().decode(data); }
		Object obj = org.springframework.util.SerializationUtils.deserialize(bytes);
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
		byte[] bytes = org.springframework.util.SerializationUtils.serialize(authorizationRequest);
		String encoded = java.util.Base64.getUrlEncoder().encodeToString(bytes);
		var map = new java.util.LinkedHashMap<String,String>();
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