package com.boika.mylocker;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AppUserRepository repository;

    public HomeController(AppUserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Principal principal, Model model) {

        AppUser user = repository.findByEmail(principal.getName()).orElse(null);

        if (user == null) {
            model.addAttribute("displayName", "there");
            model.addAttribute("firstVisit", false);
            model.addAttribute("isDemo", false);
            return "home";
        }

        boolean firstVisit = user.getLastLoginAt() == null;

        model.addAttribute("displayName", user.getUsername());
        model.addAttribute("firstVisit", firstVisit);
        model.addAttribute("isDemo", "DEMO".equals(user.getRole()));

        user.setLastLoginAt(LocalDateTime.now());
        repository.save(user);

        return "home";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

}