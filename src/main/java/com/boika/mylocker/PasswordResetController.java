package com.boika.mylocker;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {

    private static final long TOKEN_MINUTES = 30;

    private final AppUserRepository repository;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public PasswordResetController(AppUserRepository repository,
                                   PasswordEncoder encoder,
                                   EmailService emailService) {
        this.repository = repository;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendResetLink(@RequestParam String email, Model model) {

        String cleanEmail = email == null ? "" : email.trim().toLowerCase();

        repository.findByEmail(cleanEmail).ifPresent(user -> {

            if (user.isApproved()) {
                String token = UUID.randomUUID().toString();

                user.setResetToken(token);
                user.setResetTokenExpires(LocalDateTime.now().plusMinutes(TOKEN_MINUTES));
                repository.save(user);

                emailService.sendPasswordReset(user.getEmail(), user.getUsername(), token);
            }
        });

        model.addAttribute("sent", true);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam String token, Model model) {

        AppUser user = findValidUser(token);

        if (user == null) {
            model.addAttribute("invalid", true);
            return "reset-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String applyReset(@RequestParam String token,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             Model model) {

        AppUser user = findValidUser(token);

        if (user == null) {
            model.addAttribute("invalid", true);
            return "reset-password";
        }

        model.addAttribute("token", token);

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            return "reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "The two passwords do not match.");
            return "reset-password";
        }

        user.setPassword(encoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpires(null);
        repository.save(user);

        return "redirect:/login?reset";
    }

    private AppUser findValidUser(String token) {

        if (token == null || token.isBlank()) {
            return null;
        }

        AppUser user = repository.findByResetToken(token).orElse(null);

        if (user == null || user.getResetTokenExpires() == null) {
            return null;
        }

        if (LocalDateTime.now().isAfter(user.getResetTokenExpires())) {
            return null;
        }

        return user;
    }
}