package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.dto.ItemMatch;
import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.model.VerificationStatus;
import com.lostfound.lostfoundportal.service.ItemMatchingService;
import com.lostfound.lostfoundportal.service.ItemService;
import com.lostfound.lostfoundportal.service.NotificationService;
import com.lostfound.lostfoundportal.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import com.lostfound.lostfoundportal.model.User;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final ItemMatchingService itemMatchingService;
    private final NotificationService notificationService;

    public ItemController(
            ItemService itemService,
            UserService userService,
            ItemMatchingService itemMatchingService,
            NotificationService notificationService) {

        this.itemService = itemService;
        this.userService = userService;
        this.itemMatchingService = itemMatchingService;
        this.notificationService = notificationService;

    }

    @GetMapping("/lost/new")
    public String showLostForm(Model model) {

        model.addAttribute("item", new Item());

        return "add-lost-item";
    }

    @PostMapping("/lost/save")
    public String saveLostItem(
            @Valid @ModelAttribute Item item, BindingResult result,
            @RequestParam("image") MultipartFile image, Principal principal)
            throws Exception {

        if (result.hasErrors()) {
            return "add-lost-item";
        }

        if (!image.isEmpty()) {

            String fileName =
                    UUID.randomUUID() +
                    "_" +
                    image.getOriginalFilename();

            Path uploadPath =
                    Paths.get("uploads");

            Files.createDirectories(uploadPath);

            Files.copy(
                    image.getInputStream(),
                    uploadPath.resolve(fileName)
            );

            item.setImagePath(fileName);
        }

        item.setStatus("LOST");
        User user =
                userService.findByEmail(
                        principal.getName());

        item.setUser(user);

        Item savedItem = itemService.save(item);

        // Existing (already-approved) found items might already match this
        // brand-new lost report - check right away.
        notifyLikelyMatchesForNewLostItem(savedItem);

        return "redirect:/dashboard";
    }

    @GetMapping("/found/new")
    public String showFoundForm(Model model) {

        model.addAttribute("item", new Item());

        return "add-found-item";
    }

    @PostMapping("/found/save")
    public String saveFoundItem(
            @Valid @ModelAttribute("item") Item item, BindingResult result,
            @RequestParam("image") MultipartFile image, Principal principal)
            throws Exception {

        if (result.hasErrors()) {
            return "add-found-item";
        }

        if (!image.isEmpty()) {

            String fileName =
                    UUID.randomUUID() +
                    "_" +
                    image.getOriginalFilename();

            Path uploadPath =
                    Paths.get("uploads");

            Files.createDirectories(uploadPath);

            Files.copy(
                    image.getInputStream(),
                    uploadPath.resolve(fileName)
            );

            item.setImagePath(fileName);
        }

        item.setStatus("FOUND");

        // Enters the admin verification pipeline here. It won't appear on
        // the dashboard, in search, or in match results until an admin
        // advances it all the way to APPROVED (see /admin/found-items).
        item.setVerificationStatus(VerificationStatus.SUBMITTED);

        User user =
                userService.findByEmail(
                        principal.getName());

        item.setUser(user);

        itemService.save(item);

        return "redirect:/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(
            @PathVariable Long id,
            Principal principal,
            Authentication authentication) {

        Item item = itemService.getItemById(id);

        if (item == null) {
            return "redirect:/my-posts";
        }

        boolean isOwner = item.getUser() != null
                && item.getUser().getEmail().equals(principal.getName());

        boolean isModeratorOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")
                        || a.getAuthority().equals("ROLE_ADMIN"));

        // Owners can remove their own posts; MODERATOR/ADMIN can remove anyone's.
        if (!isOwner && !isModeratorOrAdmin) {
            return "redirect:/my-posts";
        }

        // No manual claim cleanup needed any more - Item.claimRequests is now
        // cascade = ALL, orphanRemoval = true, so deleting the item deletes
        // its claims automatically.
        itemService.deleteItem(id);

        return "redirect:/my-posts";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Item item =
                itemService.getItemById(id);

        model.addAttribute("item", item);

        return "edit-item";
    }

    @PostMapping("/update")
    public String updateItem(
            @ModelAttribute Item updatedItem) {

        Item existingItem =
                itemService.getItemById(
                        updatedItem.getId());

        existingItem.setItemName(
                updatedItem.getItemName());

        existingItem.setDescription(
                updatedItem.getDescription());

        existingItem.setLocation(
                updatedItem.getLocation());

        itemService.save(existingItem);

        return "redirect:/my-posts";
    }

    /**
     * A new LOST report just came in - see if any existing (already
     * APPROVED) FOUND items already look like a strong match, and email
     * this reporter if so.
     */
    private void notifyLikelyMatchesForNewLostItem(Item lostItem) {

        List<ItemMatch> matches = itemMatchingService.findMatchesForLostItem(lostItem);

        matches.stream()
                .filter(match -> match.getScore() >= ItemMatchingService.NOTIFY_THRESHOLD)
                .forEach(match -> notificationService.notifyPossibleMatch(
                        lostItem, match.getItem(), match.getScore()));
    }
}