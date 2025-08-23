package com.app.config;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
	
	@Value("${frontend.url}")
	private String frontendUrl;
	
	@Bean
	public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository crlfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        crlfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        crlfTokenRepository.setCookieCustomizer(c -> c
    		    .path("/")
    		    .sameSite("None")
    		    .secure(true));
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
						config.setAllowedOrigins(List.of(frontendUrl)); // フロントエンドのURLを許可
						config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
						config.addAllowedHeader("*"); // 全てのヘッダーを許可
						config.setAllowCredentials(true); // クッキーを許可
						return config;
					}))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/login").permitAll() // ログインページは誰でもアクセス可能
						.requestMatchers("/api/authentication").permitAll() // 認証APIは誰でもアクセス可能
						.requestMatchers("/api/AD*/**").hasRole("ADMIN") // 管理者API
						.requestMatchers("/api/ST*/**").hasRole("USER") // 生徒API
						.anyRequest().authenticated() // 他のリクエストは認証が必要
				)
				.exceptionHandling(e -> e
				          .defaultAuthenticationEntryPointFor(
				              new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
				              new org.springframework.security.web.util.matcher.RequestMatcher() {
				                  @Override
				                  public boolean matches(HttpServletRequest request) {
				                      return request.getRequestURI().startsWith("/api/");
				                  }
				              }
				          )
				      )
				      .requestCache(c -> c.disable())
				.oauth2Login(oauth -> oauth
                        .successHandler(oauth2LoginSuccessHandler) // OAuth2ログイン成功時のハンドラー)
                )
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);;
		
		return http.build();
	}
}