package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.example.controller.Security.JwtService;
import org.example.controller.Security.LoginAttemptService;
import org.example.controller.Security.TokenVersionService;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegisteredUsersForVerificationRepository registeredUsersForVerificationRepository;
    private final CoachVerifyRequestRepository coachVerifyRequestRepository;
    private final LoginAttemptService loginAttemptService;
    private final TokenVersionService tokenVersionService;

    // Wrong usernames are checked against this hash so they take as long as wrong passwords
    // (otherwise response time reveals which usernames exist).
    private final String dummyPasswordHash;

    private static final String INVALID_CREDENTIALS = "Invalid username or password.";

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RegisteredUsersForVerificationRepository registeredUsersForVerificationRepository, CoachVerifyRequestRepository coachVerifyRequestRepository, LoginAttemptService loginAttemptService, TokenVersionService tokenVersionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registeredUsersForVerificationRepository = registeredUsersForVerificationRepository;
        this.coachVerifyRequestRepository = coachVerifyRequestRepository;
        this.loginAttemptService = loginAttemptService;
        this.tokenVersionService = tokenVersionService;
        this.dummyPasswordHash = passwordEncoder.encode("timing-equalizer-" + java.util.UUID.randomUUID());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.password() == null || request.email() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username, password and e-mail are required"));
        }
        String username = request.username().toLowerCase().trim();
        String password = request.password();
        String email = request.email().trim();

        ResponseEntity<?> data = this.checkData(username, password, email);
        if (data != null) return data;

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);

        if (!registeredUsersForVerificationRepository.registerUserForVerification(newUser)) {
            return ResponseEntity.status(503).body(Map.of("message", "Registration is temporarily unavailable. Please try again later."));
        }

        return ResponseEntity.ok(Map.of("message", "Registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username and password are required"));
        }
        username = username.toLowerCase().trim();

        // Nobody has a username/password this long; don't spend bcrypt time on junk
        if (username.length() > 64 || password.length() > 128) {
            return ResponseEntity.status(401).body(Map.of("message", INVALID_CREDENTIALS));
        }

        // Per-account brute-force protection (the per-IP limit is in RateLimitFilter)
        long lockedForSeconds = loginAttemptService.secondsUntilUnlocked(username);
        if (lockedForSeconds > 0) {
            long minutes = (lockedForSeconds + 59) / 60;
            return ResponseEntity.status(429)
                    .header("Retry-After", String.valueOf(lockedForSeconds))
                    .body(Map.of("message", "Too many failed login attempts. Try again in " + minutes + (minutes == 1 ? " minute." : " minutes.")));
        }

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        // Always run one bcrypt check, even for unknown usernames, so timing doesn't leak which accounts exist
        String hash = userOpt.map(UserEntity::getPassword).orElse(dummyPasswordHash);
        boolean passwordOk = passwordEncoder.matches(password, hash);

        if (userOpt.isPresent() && passwordOk) {
            loginAttemptService.loginSucceeded(username);
            UserEntity user = userOpt.get();
            String token = jwtService.generateToken(user.getUsername(), user.getTokenVersion());
            return ResponseEntity.ok(Map.of("token", token));
        }

        // Only tell someone the account still needs e-mail verification if they know its password
        Optional<UserEntity> pending = registeredUsersForVerificationRepository.findPendingUser(username);
        if (pending.isPresent() && passwordEncoder.matches(password, pending.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "You need to verify your e-mail."));
        }

        loginAttemptService.loginFailed(username);
        // Same message for "no such user" and "wrong password", so usernames can't be enumerated
        return ResponseEntity.status(401).body(Map.of("message", INVALID_CREDENTIALS));
    }

    /**
     * Logs the current user out on every device: all tokens issued so far (web, iOS app)
     * stop working immediately. Requires a valid token.
     */
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutEverywhere() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        tokenVersionService.revokeAll(username);
        return ResponseEntity.ok(Map.of("message", "Logged out on all devices."));
    }

    @GetMapping(value = "/verify/user", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyUser(@RequestParam String code) {
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
    public ResponseEntity<String> verifyCoachingRequest(@RequestParam String code) {
        AddCoachRequest request =  coachVerifyRequestRepository.verifyUser(code);
        if (request == null){return ResponseEntity.badRequest().body(this.body_fail_coach);}

        // The request holds copies loaded when it was sent - reload so we don't save stale data
        UserEntity athlete = userRepository.findById(request.user().getId()).orElse(null);
        UserEntity coach = userRepository.findById(request.coach().getId()).orElse(null);
        if (athlete == null || coach == null){return ResponseEntity.badRequest().body(this.body_fail_coach);}

        // Already linked (e.g. two requests were sent) -> nothing to do, still a success
        if (!athlete.isCoach(coach)) {
            athlete.addCoach(coach);
            userRepository.save(athlete); // athlete owns the user_coaches link
        }

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