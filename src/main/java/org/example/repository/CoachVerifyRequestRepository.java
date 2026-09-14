package org.example.repository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.model.DTOs.AddCoachRequest;
import org.example.model.UserEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Service
public class CoachVerifyRequestRepository {
    private HashMap<Integer, AddCoachRequest> requests;
    private JavaMailSender mailSender;
    private Random random;

    public CoachVerifyRequestRepository(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.random = new Random();
        this.requests = new HashMap<>();
    }

    public void addCoachRequest(UserEntity user, UserEntity coach) {
        int verificationCode = this.random.nextInt(100000,999999);
        requests.put(verificationCode,new AddCoachRequest(user,coach));
        this.sendCoachVerificationEmail(coach.getEmail(), user.getUsername(),  verificationCode);
    }

    public AddCoachRequest verifyUser(int verificationCode) {
        if (verificationCode < 100000 || verificationCode > 999999) {return null;}
        AddCoachRequest coachRequest = this.requests.get(verificationCode);
        this.requests.remove(verificationCode);
        return coachRequest;
    }

    private void sendCoachVerificationEmail(String to, String username, int code) {
        MimeMessage message = mailSender.createMimeMessage();
        String subject = "Coaching request from " + username + " | on BLResults platform";

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

//            String baseUrl = "https://blresults.pl";
            String baseUrl = "http://localhost:8080";

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
            """.formatted(username, baseUrl, code);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Błąd podczas wysyłania e-maila", e);
        }
    }



}
