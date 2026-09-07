package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.controller.Security.JwtService;
import org.example.model.RegisterRequest;
import org.example.model.UserEntity;
import org.example.repository.RegisteredUsersForVerificationRepository;
import org.example.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegisteredUsersForVerificationRepository registeredUsersForVerificationRepository;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RegisteredUsersForVerificationRepository registeredUsersForVerificationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registeredUsersForVerificationRepository = registeredUsersForVerificationRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String username = request.username();
        String password = request.password();
        String email = request.email();

        ResponseEntity<?> data = this.checkData(username, password, email);
        if (data != null) return data;

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);

        registeredUsersForVerificationRepository.registerUserForVerification(newUser);

//        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "Registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "User doesn't exist!"));
        }

        UserEntity user = userOpt.get();


        if (passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Wrong password!"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam Integer code) {
        System.out.println("VERIFICATION CODE: " + code);
        UserEntity user =  registeredUsersForVerificationRepository.verifyUser(code);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("message", "Wrong code!"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Verified successfully!"));
    }


    private ResponseEntity<?> checkData(String username, String password, String email) {

        if(username.length() < 6) {return ResponseEntity.badRequest().body(Map.of("message", "Username must be at least 6 characters long"));}

        if(username.length() > 32) {return ResponseEntity.badRequest().body(Map.of("message", "Username is too long"));}

        if(password.length() < 6) {return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters long"));}

        if(password.length() > 32) {return ResponseEntity.badRequest().body(Map.of("message", "Password is too long"));}

        if(email.length() < 5 || !email.contains("@")){return ResponseEntity.badRequest().body(Map.of("message", "Invalid email address"));}

        if (userRepository.findByUsername(username).isPresent() || registeredUsersForVerificationRepository.isUserRegisteredForVerification(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }

        if (userRepository.findByEmail(email).isPresent() || registeredUsersForVerificationRepository.isEmailRegisteredForVerification(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use!"));
        }

        return null;
    }



}