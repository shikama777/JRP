package com.app.config;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

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

    /* ---- Forwarded ヘッダを最優先で解釈させる ---- */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilterRegistration(
            ForwardedHeaderFilter f) {
        FilterRegistrationBean<ForwardedHeaderFilter> reg = new FilterRegistrationBean<>(f);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE); // ★ 最優先
        return reg;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> sessionCookieCustomizer() {
        return factory -> factory.addContextCustomizers(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                context.setUseHttpOnly(true);                 // HttpOnly
                context.setSessionCookieDomain("next-shift-13fc0.web.app"); // ★ ここをあなたの web.app に
                context.setSessionCookiePath("/");
                // Secure / SameSite=None は properties 側で付く（下に記載）
            }
        });
    }

    /* ---- 認可開始で必ずセッションを作らせるフィルタ ---- */
    @Bean
    public SessionKickstartFilter sessionKickstartFilter() {
        return new SessionKickstartFilter();
    }

    public static class SessionKickstartFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            String uri = req.getRequestURI();
            System.out.println("[SessionKickstart] request URI: " + uri);
            if (uri.startsWith("/api/oauth2/authorization")) {
                // ★ セッションを強制生成 → Set-Cookie(JSESSIONID) を確実に出す
                req.getSession(true);
                System.out.println("[SessionKickstart] created session for " + uri
                        + " isSecure=" + req.isSecure()
                        + " scheme=" + req.getScheme()
                        + " host=" + req.getServerName());
            }
            chain.doFilter(req, res);
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SessionKickstartFilter sessionKickstartFilter) throws Exception {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName("_csrf");

        http
            // セッションは必要に応じて必ず作る
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
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
                .failureHandler((req,res,ex) -> {  // ← 一時的にログ可視化
                    ex.printStackTrace();
                    res.sendError(401, "OAuth2 failure: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                })
                .authorizationEndpoint(endpt -> endpt
                    .baseUri("/api/oauth2/authorization")
                    .authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository())
                )
                .redirectionEndpoint(redir -> redir
                    .baseUri("/api/login/oauth2/code/*")
                )
            )

            // ★ 認可開始でセッションを作るフィルタ → JWT フィルタの前に
            .addFilterBefore(sessionKickstartFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
