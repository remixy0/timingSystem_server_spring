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
    private final TokenVersionService tokenVersionService;

    public JwtAuthFilter(JwtService jwtService, TokenVersionService tokenVersionService) {
        this.jwtService = jwtService;
        this.tokenVersionService = tokenVersionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtService.parse(authHeader.substring(7))
                // Rejects revoked tokens ("log out on all devices") and tokens of deleted users
                .filter(token -> tokenVersionService.isCurrent(token.username(), token.version()))
                .ifPresent(token -> SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(token.username(), null, Collections.emptyList())));

        filterChain.doFilter(request, response);
    }
}
