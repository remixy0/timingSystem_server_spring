package org.example.controller.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Sprawdź, czy nagłówek istnieje i czy zaczyna się od "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Wytnij sam token (pomiń słowo "Bearer ")
        final String jwt = authHeader.substring(7);

        try {
            // 3. Sprawdź ważność tokenu i wyciągnij login
            if (jwtService.isTokenValid(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extractUsername(jwt);

                // 4. Stwórz obiekt uwierzytelnienia i przekaż go do kontekstu Spring Security
                // Tutaj w uproszczeniu przekazujemy pustą listę ról (Collections.emptyList())
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList()
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Zła sygnatura lub token wygasł - kontekst pozostaje pusty (błąd 403/401 rzuci Spring)
        }

        // 5. Przekaż zapytanie dalej w łańcuchu
        filterChain.doFilter(request, response);
    }
}