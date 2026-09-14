package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.example.controller.Security.JwtService;
import org.example.model.DTOs.AddCoachRequest;
import org.example.model.DTOs.RegisterRequest;
import org.example.model.UserEntity;
import org.example.repository.CoachVerifyRequestRepository;
import org.example.repository.RegisteredUsersForVerificationRepository;
import org.example.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegisteredUsersForVerificationRepository registeredUsersForVerificationRepository;
    private final CoachVerifyRequestRepository coachVerifyRequestRepository;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RegisteredUsersForVerificationRepository registeredUsersForVerificationRepository, CoachVerifyRequestRepository coachVerifyRequestRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registeredUsersForVerificationRepository = registeredUsersForVerificationRepository;
        this.coachVerifyRequestRepository = coachVerifyRequestRepository;
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

        return ResponseEntity.ok(Map.of("message", "Registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        if(registeredUsersForVerificationRepository.isUserRegisteredForVerification(username)) return ResponseEntity.status(401).body(Map.of("message", "You need to verify your e-mail."));

        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "User doesn't exist!"));

        UserEntity user = userOpt.get();

        if (passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Wrong password!"));
    }

    @GetMapping(value = "/verify/user", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyUser(@RequestParam Integer code) {
        UserEntity user =  registeredUsersForVerificationRepository.verifyUser(code);
        if (user == null){return ResponseEntity.badRequest().body(this.body_fail);}
        userRepository.save(user);
        return ResponseEntity.ok(this.body_succes);
    }


    @PostMapping("/verify/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String username) {
        if(registeredUsersForVerificationRepository.resendVerificationCode(username)){
            return ResponseEntity.ok().body(Map.of("message", "Verification code has been sent!"));
        }else{
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot resend verification code."));
        }
    }

    @Transactional
    @GetMapping(value = "/verify/coach", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyCoachingRequest(@RequestParam Integer code) {
        AddCoachRequest request =  coachVerifyRequestRepository.verifyUser(code);
        if (request == null){return ResponseEntity.badRequest().body(this.body_fail_coach);}

        request.coach().addCoachingAthlete(request.user());
        request.user().addCoach(request.coach());

        userRepository.save(request.user());
        userRepository.save(request.coach());

        return ResponseEntity.ok(this.body_succes_coach);
    }


    private String body_fail = """
            <!DOCTYPE html>
            <html>
            <body style="background-color: #0b102e; color: #ffffff; font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;">
                <div style="background-color: #12193e; padding: 40px; border-radius: 12px; text-align: center; border: 1px solid #1e285a; max-width: 400px;">
                    <h1 style="color: #ff4d4d; margin-top: 0;">Verification Failed</h1>
                    <p style="color: #94a3b8;">The verification code is invalid or has expired.</p>
                </div>
            </body>
            </html>
            """;

    private String body_succes = """
        <!DOCTYPE html>
        <html>
        <body style="background-color: #0b102e; color: #ffffff; font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;">
            <div style="background-color: #12193e; padding: 40px; border-radius: 12px; text-align: center; border: 1px solid #1e285a; max-width: 400px;">
                <h1 style="color: #2ecc71; margin-top: 0;">Account Verified!</h1>
                <p style="color: #94a3b8;">Your account is now active. You can close this tab and log in.</p>
            </div>
        </body>
        </html>
        """;

    private String body_fail_coach = """
            <!DOCTYPE html>
            <html>
            <body style="background-color: #0b102e; color: #ffffff; font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;">
                <div style="background-color: #12193e; padding: 40px; border-radius: 12px; text-align: center; border: 1px solid #1e285a; max-width: 400px;">
                    <h1 style="color: #ff4d4d; margin-top: 0;">Request failed</h1>
                    <p style="color: #94a3b8;">The request is invalid or has expired.</p>
                </div>
            </body>
            </html>
            """;

    private String body_succes_coach = """
        <!DOCTYPE html>
        <html>
        <body style="background-color: #0b102e; color: #ffffff; font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;">
            <div style="background-color: #12193e; padding: 40px; border-radius: 12px; text-align: center; border: 1px solid #1e285a; max-width: 400px;">
                <h1 style="color: #2ecc71; margin-top: 0;">Accepted coaching request!</h1>
                <p style="color: #94a3b8;">You can close this tab.</p>
            </div>
        </body>
        </html>
        """;

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