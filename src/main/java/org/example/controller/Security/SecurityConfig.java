package org.example.controller.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Wyłączamy CSRF, ponieważ w API opartym na JWT (i braku ciasteczek sesyjnych) nie jesteśmy na to podatni
                .csrf(csrf -> csrf.disable())
                // 2. Ustalamy politykę sesji na BEZSTANOWĄ (brak JSESSIONID)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3. Konfigurujemy dostęp do endpointów
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll() // Logowanie jest dostępne dla każdego
                        .anyRequest().authenticated()              // Reszta wymaga JWT
                )
                // 4. Dodajemy nasz filtr JWT przed standardowym filtrem sprawdzającym login i hasło
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
