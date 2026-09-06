package org.example.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String userName) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                        <h2 style="color: #2c3e50; margin-bottom: 20px;">Cześć %s! 👋</h2>
                        <p style="color: #555555; line-height: 1.6;">
                            Dziękujemy za dołączenie do naszej aplikacji. Twoje konto jest gotowe do użycia.
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="https://example.com/confirm" 
                               style="background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                Aktywuj konto
                            </a>
                        </div>
                        <hr style="border: none; border-top: 1px solid #eee; margin-top: 30px;">
                        <p style="color: #aaa; font-size: 12px; text-align: center;">
                            Wiadomość wygenerowana automatycznie. Nie odpowiadaj na ten e-mail.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(userName);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Błąd podczas wysyłania e-maila", e);
        }

        mailSender.send(message);
    }
}
