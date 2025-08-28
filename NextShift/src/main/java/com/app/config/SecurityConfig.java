package com.app.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

import com.app.security.HttpCookieOAuth2AuthorizationRequestRepository;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName("_csrf");

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(frontendUrl));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.addAllowedHeader("*");
                config.setAllowCredentials(true);
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/oauth2/authorization/**", "/api/login/oauth2/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/refresh").permitAll()
                .requestMatchers("/api/AD*/**").hasRole("ADMIN")
                .requestMatchers("/api/ST*/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .requestCache(c -> c.disable())
            .oauth2Login(oauth -> oauth
                .successHandler(oauth2LoginSuccessHandler)
                .authorizationEndpoint(endpt -> endpt
                    .baseUri("/api/oauth2/authorization")
                    .authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository())
                )
                .redirectionEndpoint(redir -> redir
                    .baseUri("/api/login/oauth2/code/*")
                )
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
