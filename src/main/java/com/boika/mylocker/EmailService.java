package com.boika.mylocker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${mylocker.admin.email:}")
    private String adminEmail;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${mylocker.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPasswordReset(String toEmail, String displayName, String token) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("HopeConnect: reset your password");
            message.setText(
                    "Hello " + displayName + ",\n\n"
                            + "Someone asked to reset the password for your HopeConnect account.\n\n"
                            + "Click the link below to choose a new password:\n"
                            + baseUrl + "/reset-password?token=" + token + "\n\n"
                            + "This link expires in 30 minutes and can only be used once.\n\n"
                            + "If you did not ask for this, ignore this email. "
                            + "Your password will not change.\n");

            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);

        } catch (Exception e) {
            log.warn("Could not send password reset email: {}", e.getMessage());
        }
    }

    @Async
    public void sendInvite(String toEmail, String displayName, String token) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("You have been invited to HopeConnect");
            message.setText(
                    "Hello " + displayName + ",\n\n"
                            + "An account has been created for you on HopeConnect,\n"
                            + "a private file and document store.\n\n"
                            + "Set your password using the link below:\n"
                            + baseUrl + "/reset-password?token=" + token + "\n\n"
                            + "This link expires in 30 minutes. If it expires, use the\n"
                            + "'Forgot your password' link on the sign-in page to get a new one.\n\n"
                            + "You will sign in with this email address: " + toEmail + "\n");

            mailSender.send(message);
            log.info("Invite email sent to {}", toEmail);

        } catch (Exception e) {
            log.warn("Could not send invite email: {}", e.getMessage());
        }
    }

    @Async
    public void sendContactMessage(String fromName, String fromEmail, String body) {

        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("No admin email configured. Skipping contact message.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(adminEmail);
            message.setReplyTo(fromEmail);
            message.setSubject("HopeConnect contact form: " + fromName);
            message.setText(
                    "New message from the HopeConnect contact form.\n\n"
                            + "Name: " + fromName + "\n"
                            + "Email: " + fromEmail + "\n\n"
                            + "Message:\n"
                            + body + "\n");

            mailSender.send(message);
            log.info("Contact message forwarded from {}", fromEmail);

        } catch (Exception e) {
            log.warn("Could not send contact message: {}", e.getMessage());
        }
    }
}