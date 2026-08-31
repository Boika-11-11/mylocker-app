package com.boika.mylocker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final AppUserRepository repository;

    public AdminController(AppUserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/admin")
    public String showPending(Model model) {
        model.addAttribute("pending", repository.findByApprovedFalse());
        return "admin";
    }

    @PostMapping("/admin/approve")
    public String approve(@RequestParam Long id) {
        repository.findById(id).ifPresent(user -> {
            user.setApproved(true);
            repository.save(user);
        });
        return "redirect:/admin";
    }

    @PostMapping("/admin/reject")
    public String reject(@RequestParam Long id) {
        repository.deleteById(id);
        return "redirect:/admin";
    }
}