package org.example.controller.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Content-Security-Policy for API responses. The API only returns JSON and the small
     * verification pages (which use inline styles), so nothing else may load or run.
     */
    private static final String API_CSP =
            "default-src 'none'; style-src 'unsafe-inline'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'";

    private static final String PERMISSIONS_POLICY =
            "camera=(), microphone=(), geolocation=(), payment=(), usb=()";

    private final JwtAuthFilter jwtAuthFilter;
    // These two are not @Components on purpose: they should only run inside this security chain
    // (after CORS, so browsers can read the 429/413 message), not a second time as servlet filters.
    private final RateLimitFilter rateLimitFilter = new RateLimitFilter(new RateLimiter());
    private final RequestSizeLimitFilter requestSizeLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          @Value("${app.limits.max-request-bytes:10485760}") long maxRequestBytes,   // 10 MB
                          @Value("${app.limits.max-upload-bytes:104857600}") long maxUploadBytes) {  // 100 MB (athletes with photos)
        this.jwtAuthFilter = jwtAuthFilter;
        this.requestSizeLimitFilter = new RequestSizeLimitFilter(maxRequestBytes, maxUploadBytes);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Swagger UI needs scripts, so it doesn't get the strict API CSP (it is disabled in production anyway)
        RequestMatcher notSwagger = request -> {
            String path = request.getRequestURI();
            return !(path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs"));
        };

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Missing/expired/revoked token -> 401 (was 403), so clients know to log in again
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Spring already sends X-Content-Type-Options, X-Frame-Options: DENY, Cache-Control: no-store
                // and (over HTTPS) Strict-Transport-Security. These add the rest:
                .headers(headers -> headers
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY))
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(notSwagger,
                                new StaticHeadersWriter("Content-Security-Policy", API_CSP)))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/register","/api/verify/user","/api/verify/resend","/api/verify/coach",
                                "/ws/**",
                                "/swagger-ui/index.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestSizeLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
