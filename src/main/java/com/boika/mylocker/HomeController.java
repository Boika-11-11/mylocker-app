package com.boika.mylocker;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHome() {
        return "home";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

}