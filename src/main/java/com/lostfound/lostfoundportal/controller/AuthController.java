package com.lostfound.lostfoundportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.lostfound.lostfoundportal.model.User;
import com.lostfound.lostfoundportal.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute User user) {

        userService.registerUser(user);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {

        return "login";
    }
}