package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.service.ItemVerificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The admin found-item verification queue. Restricted to ADMIN both at the
 * URL level (SecurityConfig already maps "/admin/**" -> hasRole("ADMIN"))
 * and again here with @PreAuthorize, same pattern as AdminController.
 */
@Controller
@RequestMapping("/admin/found-items")
@PreAuthorize("hasRole('ADMIN')")
public class AdminItemVerificationController {

    private final ItemVerificationService itemVerificationService;

    public AdminItemVerificationController(ItemVerificationService itemVerificationService) {
        this.itemVerificationService = itemVerificationService;
    }

    @GetMapping
    public String showQueue(Model model) {

        model.addAttribute("items", itemVerificationService.getFoundItemsForReview());

        return "admin-found-items";
    }

    @PostMapping("/{id}/advance")
    public String advance(@PathVariable Long id) {

        itemVerificationService.advanceVerification(id);

        return "redirect:/admin/found-items";
    }
}