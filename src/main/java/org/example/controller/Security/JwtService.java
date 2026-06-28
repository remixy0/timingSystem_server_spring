package org.example.controller.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtService {

    // W produkcji klucz powinien być wstrzykiwany z application.properties!
    // Musi być wystarczająco długi dla algorytmu HS256 (min. 256 bitów).
    private final String SECRET = "ToJestBardzoTajnyKluczDoPodpisywaniaTokenowJwtKtoryMusiBycDlugii";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Generowanie tokenu
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // Ważny 1 godzinę
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Weryfikacja i pobranie nazwy użytkownika
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Sprawdzenie czy token jest poprawny
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}