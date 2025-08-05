package com.app.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
   @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.
			    cors(cors -> cors.configurationSource(request ->  {
						CorsConfiguration config = new CorsConfiguration();
						config.setAllowedOrigins(List.of("http://localhost:5173")); 
						config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
						config.addAllowedHeader("*"); // 全てのヘッダーを許可
						config.setAllowCredentials(true); // クッキーを許可
						return config;
					})).
				authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/login").permitAll() // ログインページは誰でもアクセス可能
						.anyRequest().authenticated() // 他のリクエストは認証が必要
				)
				.oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("http://localhost:5173/AD")
                );
		
		return http.build();
	}
}