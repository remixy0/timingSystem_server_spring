package org.example.repository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.util.Optional;

@Service
public class RegisteredUsersForVerificationRepository {
    /** How long a "verify your e-mail" link works. */
    private static final Duration LINK_VALID_FOR = Duration.ofHours(24);
    /** Upper limit of accounts waiting for verification at the same time (protects memory). */
    private static final int MAX_PENDING = 10_000;

    private final PendingTokenStore<UserEntity> pending = new PendingTokenStore<>(LINK_VALID_FOR, MAX_PENDING);
    private final JavaMailSender mailSender;
    @Value("${app.base.url}")
    private String baseUrl;

    public RegisteredUsersForVerificationRepository(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    /** @return false if too many registrations are waiting right now (nothing is sent). */
    public boolean registerUserForVerification(UserEntity user){
        Optional<String> token = pending.add(user);
        if (token.isEmpty()) return false;
        this.sendVerificationEmail(user.getEmail(), user.getUsername(), token.get());
        return true;
    }

    public Optional<UserEntity> findPendingUser(String username) {
        if (username == null) return Optional.empty();
        return pending.find(user -> username.equals(user.getUsername()));
    }

    /** Returns the user for a valid, unused, unexpired link token - otherwise null. Each token works once. */
    public UserEntity verifyUser(String token){
        return pending.take(token);
    }

    public boolean resendVerificationCode(String username) {
        if (username == null) return false;

        Optional<PendingTokenStore.Issued<UserEntity>> issued =
                pending.reissue(user -> username.equals(user.getUsername()));
        if (issued.isEmpty()) return false;

        UserEntity user = issued.get().value();
        this.sendVerificationEmail(user.getEmail(), user.getUsername(), issued.get().token());
        return true;
    }

    public boolean isUserRegisteredForVerification(String username) {
        if (username == null) return false;
        return pending.contains(user -> username.equals(user.getUsername()));
    }

    public boolean isEmailRegisteredForVerification(String email) {
        if (email == null) return false;
        return pending.contains(user -> email.equalsIgnoreCase(user.getEmail()));
    }

    private void sendVerificationEmail(String to, String userName, String code) {
        MimeMessage message = mailSender.createMimeMessage();
        String subject = "Verification Email for BLTiming";

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

//            String baseUrl = "https://blresults.pl";


            String htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0b102e; margin: 0; padding: 40px 20px;">
                <div style="max-width: 500px; margin: 0 auto; background-color: #12193e; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.5); border: 1px solid #1e285a;">
                       <div style="padding: 40px 30px;">
                        <h2 style="color: #ffffff; margin-top: 0; margin-bottom: 16px; font-size: 22px; font-weight: 700;">Hi %s!</h2>
                        <p style="color: #94a3b8; line-height: 1.6; font-size: 15px; margin-bottom: 30px;">
                            Thank you for signing up. Your account is almost ready. Please confirm your email address by clicking the button below.
                        </p>
                        
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s/api/verify/user?code=%s" 
                               style="background-color: #2b66ff; color: #ffffff; padding: 14px 32px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px; display: inline-block;">
                                Verify Account
                            </a>
                        </div>
        
                        <hr style="border: none; border-top: 1px solid #1e285a; margin: 35px 0 25px 0;">
                        
                        <p style="color: #64748b; font-size: 12px; text-align: center; margin: 0;">
                            This is an automated message, please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(HtmlUtils.htmlEscape(userName), baseUrl, code); // escape: usernames could contain HTML

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Błąd podczas wysyłania e-maila", e);
        }
    }

}
