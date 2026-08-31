package com.boika.mylocker;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignupController {

    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    public SignupController(AppUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @GetMapping("/signup")
    public String showForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             Model model) {

        String cleanUsername = username == null ? "" : username.trim();

        model.addAttribute("username", cleanUsername);

        if (cleanUsername.isBlank() || cleanUsername.length() > 30) {
            model.addAttribute("error", "Username must be 1 to 30 characters.");
            return "signup";
        }

        if (!cleanUsername.matches("^[a-zA-Z0-9._-]+$")) {
            model.addAttribute("error", "Username may only contain letters, numbers, dot, dash or underscore.");
            return "signup";
        }

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            return "signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "The two passwords do not match.");
            return "signup";
        }

        if (repository.findByUsername(cleanUsername).isPresent()) {
            model.addAttribute("error", "That username is taken.");
            return "signup";
        }

        AppUser user = new AppUser();
        user.setUsername(cleanUsername);
        user.setPassword(encoder.encode(password));
        user.setRole("USER");
        user.setApproved(false);

        repository.save(user);

        return "redirect:/login?registered";
    }
}