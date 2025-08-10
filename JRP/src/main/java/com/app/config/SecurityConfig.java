package com.app.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

import com.app.security.JwtAuthenticationFilter;
import com.app.security.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
	
	@Bean
	public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository crlfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        crlfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        crlfTokenRepository.setCookieCustomizer(c -> c
    		    .path("/")
    		    .sameSite("Lax")
    		    .secure(false));
        return crlfTokenRepository;
    }

   @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	   CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
	    handler.setCsrfRequestAttributeName("_csrf");
	    
		http
			.csrf(csrf -> csrf
		            .csrfTokenRepository(csrfTokenRepository())
		            .csrfTokenRequestHandler(handler) // CSRFトークンのリクエスト属性名を設定
		        )
			    .cors(cors -> cors.configurationSource(request ->  {
						CorsConfiguration config = new CorsConfiguration();
						config.setAllowedOrigins(List.of("http://localhost:5173")); 
						config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
						config.addAllowedHeader("*"); // 全てのヘッダーを許可
						config.setAllowCredentials(true); // クッキーを許可
						return config;
					})).
				authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/login").permitAll() // ログインページは誰でもアクセス可能
						.requestMatchers("/api/authentication").permitAll() // 認証APIは誰でもアクセス可能
						.requestMatchers("/api/AD*/**").hasRole("ADMIN") // 管理者API
						.requestMatchers("/api/ST*/**").hasRole("USER") // 生徒API
						.anyRequest().authenticated() // 他のリクエストは認証が必要
				)
				.oauth2Login(oauth -> oauth
                        .successHandler(oauth2LoginSuccessHandler) // OAuth2ログイン成功時のハンドラー)
                )
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);;
		
		return http.build();
	}
}