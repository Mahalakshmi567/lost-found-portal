package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.dto.ItemMatch;
import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.service.ItemMatchingService;
import com.lostfound.lostfoundportal.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/matches")
public class MatchController {

    private final ItemService itemService;
    private final ItemMatchingService itemMatchingService;

    public MatchController(ItemService itemService, ItemMatchingService itemMatchingService) {
        this.itemService = itemService;
        this.itemMatchingService = itemMatchingService;
    }

    @GetMapping("/{id}")
    public String showMatches(
            @PathVariable Long id,
            Principal principal,
            Model model) {

        Item lostItem = itemService.getItemById(id);

        if (lostItem == null || !"LOST".equals(lostItem.getStatus())) {
            return "redirect:/my-posts";
        }

        boolean isOwner = lostItem.getUser() != null
                && lostItem.getUser().getEmail().equals(principal.getName());

        // Matches are only shown to the person who reported the lost item.
        if (!isOwner) {
            return "redirect:/my-posts";
        }

        List<ItemMatch> matches = itemMatchingService.findMatchesForLostItem(lostItem);

        model.addAttribute("lostItem", lostItem);
        model.addAttribute("matches", matches);

        return "item-matches";
    }
}