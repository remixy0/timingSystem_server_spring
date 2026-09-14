package org.example.repository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.model.UserEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class RegisteredUsersForVerificationRepository {
    private Map<Integer,UserEntity> users;
    private final JavaMailSender mailSender;
    private Random random;

    public RegisteredUsersForVerificationRepository(JavaMailSender mailSender){
        this.users = new HashMap<>();
        this.mailSender = mailSender;
        this.random = new Random();
    }

    public void registerUserForVerification(UserEntity user){
        int userCode = this.random.nextInt(100000,999999);
        this.users.put(userCode,user);
        this.sendVerificationEmail(user.getEmail(), user.getUsername(), userCode);
    }

    public UserEntity verifyUser(int code){
        UserEntity user = this.users.get(code);
        this.users.remove(code);
        return user;
    }

    public boolean resendVerificationCode(String username) {
        if (username == null) return false;

        Optional<Map.Entry<Integer, UserEntity>> entryOpt = this.users.entrySet().stream()
                .filter(entry -> username.equals(entry.getValue().getUsername()))
                .findFirst();

        if (entryOpt.isEmpty()) return false;

        Map.Entry<Integer, UserEntity> entry = entryOpt.get();
        Integer oldCode = entry.getKey();
        UserEntity user = entry.getValue();

        this.users.remove(oldCode);

        int newCode = this.random.nextInt(100000, 999999);
        this.users.put(newCode, user);

        this.sendVerificationEmail(user.getEmail(), user.getUsername(), newCode);

        return true;
    }

    public boolean isUserRegisteredForVerification(String username) {
        if (username == null) return false;

        return this.users.values().stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    public boolean isEmailRegisteredForVerification(String email) {
        if (email == null) return false;

        return this.users.values().stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }

    private void sendVerificationEmail(String to, String userName, int code) {
        MimeMessage message = mailSender.createMimeMessage();
        String subject = "Verification Email for BLTiming";

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            String baseUrl = "https://blresults.pl";

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
                            <a href="%s/api/verify?code=%s" 
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
            """.formatted(userName, baseUrl, code);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Błąd podczas wysyłania e-maila", e);
        }
    }

}
