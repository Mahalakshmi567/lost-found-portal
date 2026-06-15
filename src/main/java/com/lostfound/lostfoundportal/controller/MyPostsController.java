package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.model.User;
import com.lostfound.lostfoundportal.service.ItemService;
import com.lostfound.lostfoundportal.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.lostfound.lostfoundportal.model.Item;

import java.security.Principal;

@Controller
public class MyPostsController {

    private final ItemService itemService;
    private final UserService userService;

    public MyPostsController(
            ItemService itemService,
            UserService userService) {

        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping("/my-posts")
    public String myPosts(
            Principal principal,
            Model model) {

        User user =
                userService.findByEmail(
                        principal.getName());

        model.addAttribute(
                "items",
                itemService.getItemsByUser(user));

        return "my-posts";
    }
    @GetMapping("/claims/new/{id}")
public String newClaim(@PathVariable Long id, Model model) {
    Item item = itemService.findById(id);
    if (item == null) {
        throw new IllegalArgumentException("Item not found: " + id);
    }
    model.addAttribute("item", item);
    return "claim-item"; // must exist
}

}