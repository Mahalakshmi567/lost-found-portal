package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.model.Role;
import com.lostfound.lostfoundportal.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * User & role management, restricted to ADMIN accounts.
 * URL-level restriction lives in SecurityConfig ("/admin/**" -> hasRole("ADMIN")).
 * The @PreAuthorize below is a second, method-level check on top of that -
 * belt and suspenders, and demonstrates both styles of access control.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", Role.values());

        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        userService.updateUserRole(id, role);

        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "redirect:/admin/users";
    }
}