package org.example.repository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.model.DTOs.AddCoachRequest;
import org.example.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.util.Optional;

@Service
public class CoachVerifyRequestRepository {
    /** How long a coach has to accept a request. */
    private static final Duration LINK_VALID_FOR = Duration.ofDays(7);
    /** Upper limit of open coach requests at the same time (protects memory). */
    private static final int MAX_PENDING = 10_000;

    private final PendingTokenStore<AddCoachRequest> requests = new PendingTokenStore<>(LINK_VALID_FOR, MAX_PENDING);
    private final JavaMailSender mailSender;

    @Value("${app.base.url}")
    private String baseUrl;

    public CoachVerifyRequestRepository(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** @return false if too many requests are open right now (nothing is sent). */
    public boolean addCoachRequest(UserEntity user, UserEntity coach) {
        Optional<String> token = requests.add(new AddCoachRequest(user, coach));
        if (token.isEmpty()) return false;
        this.sendCoachVerificationEmail(coach.getEmail(), user.getUsername(), token.get());
        return true;
    }

    /** Returns the request for a valid, unused, unexpired link token - otherwise null. Each token works once. */
    public AddCoachRequest verifyUser(String token) {
        return requests.take(token);
    }

    private void sendCoachVerificationEmail(String to, String username, String code) {
        MimeMessage message = mailSender.createMimeMessage();
        String subject = "Coaching request from " + username.replaceAll("[\\r\\n]", " ") + " | on BLResults platform";

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
                        <h2 style="color: #ffffff; margin-top: 0; margin-bottom: 16px; font-size: 22px; font-weight: 700;">Coaching Request</h2>
                        <p style="color: #94a3b8; line-height: 1.6; font-size: 15px; margin-bottom: 30px;">
                            <strong style="color: #ffffff;">%s</strong> has requested you as their coach on BLTiming. Please confirm this coaching relationship by clicking the button below.
                        </p>
                        
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s/api/verify/coach?code=%s" 
                               style="background-color: #2b66ff; color: #ffffff; padding: 14px 32px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px; display: inline-block;">
                                Accept Coaching Request
                            </a>
                        </div>
            
                        <hr style="border: none; border-top: 1px solid #1e285a; margin: 35px 0 25px 0;">
                        
                        <p style="color: #64748b; font-size: 12px; text-align: center; margin: 0;">
                            If you weren't expecting this request, you can safely ignore this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(HtmlUtils.htmlEscape(username), baseUrl, code); // escape: usernames could contain HTML

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Błąd podczas wysyłania e-maila", e);
        }
    }



}
