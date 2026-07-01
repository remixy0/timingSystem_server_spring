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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if(username.length() < 6) {return ResponseEntity.badRequest().body(Map.of("message", "Username must be at least 6 characters long"));}

        if(username.length() > 32) {return ResponseEntity.badRequest().body(Map.of("message", "Username is too long"));}

        if(password.length() < 6) {return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters long"));}

        if(password.length() > 32) {return ResponseEntity.badRequest().body(Map.of("message", "Password is too long"));}


        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));

        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "Registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found!"));


        if (passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Wrong password!"));
    }
}