package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.model.User;
import com.lostfound.lostfoundportal.security.JwtAuthenticationFilter;
import com.lostfound.lostfoundportal.security.JwtService;
import com.lostfound.lostfoundportal.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
    public String loginPage(
            @RequestParam(required = false) String error,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

        } catch (AuthenticationException ex) {
            return "redirect:/login?error";
        }

        User user = userService.findByEmail(username);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        Cookie authCookie = new Cookie(JwtAuthenticationFilter.COOKIE_NAME, token);
        authCookie.setHttpOnly(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(24 * 60 * 60); // 1 day, matches app.jwt.expiration-ms default

        response.addCookie(authCookie);

        return "redirect:/dashboard";
    }
}