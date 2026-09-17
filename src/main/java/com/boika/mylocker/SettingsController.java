package com.boika.mylocker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String showSettings(Model model) {
        model.addAttribute("currentPage", "settings");
        return "settings";
    }
}
