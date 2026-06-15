package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.service.ClaimRequestService;
import com.lostfound.lostfoundportal.service.ItemService;
import com.lostfound.lostfoundportal.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;
import com.lostfound.lostfoundportal.model.User;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
@Controller
@RequestMapping("/items")
public class ItemController {
private final ClaimRequestService claimRequestService;
  private final ItemService itemService;
private final UserService userService;
public ItemController(ItemService itemService,UserService userService,ClaimRequestService claimRequestService ) {

    this.itemService = itemService;
     this.userService = userService;
     this.claimRequestService= claimRequestService;
    
}
    @GetMapping("/lost/new")
    public String showLostForm(Model model) {

        model.addAttribute("item", new Item());

        return "add-lost-item";
    }

    @PostMapping("/lost/save")
public String saveLostItem(
         @Valid@ModelAttribute Item item,BindingResult result,
        @RequestParam("image") MultipartFile image,Principal principal)
        throws Exception {
        

            System.out.println("File Name: " + image.getOriginalFilename());
            System.out.println("Is Empty: " + image.isEmpty());
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

    itemService.save(item);

    return "redirect:/dashboard";
}
    @GetMapping("/found/new")
public String showFoundForm(Model model) {

    model.addAttribute("item", new Item());

    return "add-found-item";
}

@PostMapping("/found/save")
public String saveFoundItem(
       @Valid @ModelAttribute("item") Item item,BindingResult result,
        @RequestParam("image") MultipartFile image,Principal principal)
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
    User user =
        userService.findByEmail(
                principal.getName());

item.setUser(user);

    itemService.save(item);

    return "redirect:/dashboard";
}
@GetMapping("/delete/{id}")
public String deleteItem(
        @PathVariable Long id) {

    Item item =
            itemService.getItemById(id);

    claimRequestService
            .deleteClaimsByItem(item);

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
}