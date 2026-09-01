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

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void notifyAdminOfSignup(String newUsername) {

        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("No admin email configured. Skipping notification.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(adminEmail);
            message.setSubject("HopeConnect: new sign-up awaiting approval");
            message.setText(
                    "A new person has registered on HopeConnect.\n\n"
                            + "Username: " + newUsername + "\n\n"
                            + "They cannot sign in until you approve them.\n"
                            + "Open the admin page to approve or reject:\n"
                            + "http://localhost:8080/admin\n");

            mailSender.send(message);

            log.info("Sign-up notification sent for user {}", newUsername);

        } catch (Exception e) {
            log.warn("Could not send sign-up notification: {}", e.getMessage());
        }
    }
}