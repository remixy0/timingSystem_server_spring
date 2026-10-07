package org.example.controller.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService {

    /** Name of the claim holding the user's token version (see TokenVersionService). */
    private static final String VERSION_CLAIM = "ver";

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${app.secret-key}") String secret,
                      @Value("${app.jwt.expiration-hours:12}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationHours * 60 * 60 * 1000;
    }

    /** What a valid token says: who it belongs to and which token version it was issued with. */
    public record TokenData(String username, int version) {}

    public String generateToken(String username, int tokenVersion) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim(VERSION_CLAIM, tokenVersion)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Checks signature and expiry. Empty if the token is invalid in any way. */
    public Optional<TokenData> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getSubject() == null) return Optional.empty();
            // Tokens issued before versions existed have no claim - they count as version 0
            Integer version = claims.get(VERSION_CLAIM, Integer.class);
            return Optional.of(new TokenData(claims.getSubject(), version == null ? 0 : version));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
