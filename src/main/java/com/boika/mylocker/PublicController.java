package com.boika.mylocker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicController {

    private final EmailService emailService;
    private final LoginAttemptService attemptService;

    public PublicController(EmailService emailService,
                            LoginAttemptService attemptService) {
        this.emailService = emailService;
        this.attemptService = attemptService;
    }

    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @PostMapping("/contact")
    public String sendMessage(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String message,
                              @RequestParam(required = false) String website,
                              Model model) {

        if (website != null && !website.isBlank()) {
            model.addAttribute("sent", true);
            return "contact";
        }

        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = email == null ? "" : email.trim();
        String cleanMessage = message == null ? "" : message.trim();

        if (cleanName.isBlank() || cleanName.length() > 60) {
            model.addAttribute("error", "Please enter your name.");
            return "contact";
        }

        if (!cleanEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || cleanEmail.length() > 100) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "contact";
        }

        if (cleanMessage.isBlank() || cleanMessage.length() > 2000) {
            model.addAttribute("error", "Message must be between 1 and 2000 characters.");
            return "contact";
        }

        if (attemptService.isBlocked("contact:" + cleanEmail)) {
            model.addAttribute("error", "Too many messages sent. Please try again later.");
            return "contact";
        }

        attemptService.loginFailed("contact:" + cleanEmail);

        emailService.sendContactMessage(cleanName, cleanEmail, cleanMessage);

        model.addAttribute("sent", true);
        return "contact";
    }
}