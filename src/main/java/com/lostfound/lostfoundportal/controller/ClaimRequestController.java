package com.lostfound.lostfoundportal.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.ui.Model;
import com.lostfound.lostfoundportal.model.ClaimRequest;

import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.model.User;

import com.lostfound.lostfoundportal.service.ClaimRequestService;
import com.lostfound.lostfoundportal.service.ItemService;
import com.lostfound.lostfoundportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/claims")
public class ClaimRequestController {
        private final ClaimRequestService claimService;
private final ItemService itemService;
private final UserService userService;
public ClaimRequestController(
        ClaimRequestService claimService,
        ItemService itemService,
        UserService userService) {

    this.claimService = claimService;
    this.itemService = itemService;
    this.userService = userService;
}

    @GetMapping("/new/request/{itemId}")
public String showClaimForm(
        @PathVariable Long itemId,
        Model model) {

    model.addAttribute("claimRequest",
            new ClaimRequest());

    model.addAttribute("itemId",
            itemId);

    return "claim-item";
}
@PostMapping("/save/{itemId}")
public String saveClaim(
        @PathVariable Long itemId,
      @Valid @ModelAttribute("claimRequest")
ClaimRequest claimRequest,BindingResult result,
        Principal principal,Model model) {
if (result.hasErrors()) {

    model.addAttribute("itemId", itemId);

    return "claim-item";
}
    Item item =
            itemService.getItemById(itemId);

    User user =
            userService.findByEmail(
                    principal.getName());

    claimRequest.setItem(item);
    claimRequest.setUser(user);

    claimService.save(claimRequest);

    return "redirect:/dashboard";
}
@GetMapping("/item/{itemId}")
public String viewClaims(@PathVariable Long itemId, Model model) {
    Item item = itemService.getItemById(itemId);
    if (item == null) {
        return "error-page"; // custom error template
    }
    model.addAttribute("claims", claimService.getClaimsByItem(item));
    model.addAttribute("item", item);
    return "view-claims";
}

}