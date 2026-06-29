package org.example.controller;

import org.example.controller.Security.JwtService;
import org.example.model.UserEntity;
import org.example.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // --- 1. REJESTRACJA NOWEGO KONTA ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Sprawdź czy użytkownik już istnieje
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Użytkownik o takiej nazwie już istnieje!"));
        }

        // Tworzymy nowego użytkownika i SZYFRUJEMY hasło
        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // Rejestracja z bezpiecznym hashem

        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "Rejestracja pomyślna. Możesz się zalogować."));
    }

    // --- 2. LOGOWANIE (Pobieranie z bazy danych) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Szukamy użytkownika w bazie
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        // Sprawdzamy czy podane czyste hasło pasuje do hashu z bazy danych
        if (passwordEncoder.matches(password, user.getPassword())) {
            // Generujemy token (jako identyfikator zaszywamy nazwę użytkownika lub jego ID)
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Błędne hasło!"));
    }
}