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
    private final AccessRequestRepository requestRepository;
    private final AppUserRepository userRepository;

    public PublicController(EmailService emailService,
                            LoginAttemptService attemptService,
                            AccessRequestRepository requestRepository,
                            AppUserRepository userRepository) {
        this.emailService = emailService;
        this.attemptService = attemptService;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
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

    @GetMapping("/request-access")
    public String showRequestForm() {
        return "request-access";
    }

    @PostMapping("/request-access")
    public String submitRequest(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam(required = false) String reason,
                                @RequestParam(required = false) String website,
                                Model model) {

        if (website != null && !website.isBlank()) {
            model.addAttribute("sent", true);
            return "request-access";
        }

        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        String cleanReason = reason == null ? "" : reason.trim();

        model.addAttribute("name", cleanName);
        model.addAttribute("email", cleanEmail);

        if (cleanName.isBlank() || cleanName.length() > 30
                || !cleanName.matches("^[a-zA-Z0-9 ._-]+$")) {
            model.addAttribute("error",
                    "Name must be 1 to 30 letters, numbers, spaces, dot, dash or underscore.");
            return "request-access";
        }

        if (!cleanEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || cleanEmail.length() > 100) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "request-access";
        }

        if (cleanReason.length() > 500) {
            model.addAttribute("error", "Reason must be under 500 characters.");
            return "request-access";
        }

        if (attemptService.isBlocked("request:" + cleanEmail)) {
            model.addAttribute("error", "Too many requests. Please try again later.");
            return "request-access";
        }

        attemptService.loginFailed("request:" + cleanEmail);

        boolean alreadyRequested = requestRepository.existsByEmail(cleanEmail);
        boolean alreadyHasAccount = userRepository.findByEmail(cleanEmail).isPresent();

        if (!alreadyRequested && !alreadyHasAccount) {

            AccessRequest request = new AccessRequest();
            request.setEmail(cleanEmail);
            request.setName(capitalise(cleanName));
            request.setReason(cleanReason.isBlank() ? null : cleanReason);

            requestRepository.save(request);

            emailService.notifyAdminOfRequest(request.getName(), cleanEmail, cleanReason);
        }

        model.addAttribute("sent", true);
        return "request-access";
    }

    private String capitalise(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}