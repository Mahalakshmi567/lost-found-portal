package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class DashboardController {

    private final ItemService itemService;

 
    public DashboardController(ItemService itemService) {
        this.itemService = itemService;
        
    }
    
    @GetMapping("/dashboard")
public String dashboard(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        Model model) {
            System.out.println("Keyword = " + keyword);
System.out.println("Status = " + status);

    if (keyword != null && !keyword.trim().isEmpty()) {

        model.addAttribute(
                "items",
                itemService.searchItems(keyword)
        );

    } else if (status != null && !status.trim().isEmpty()) {

        model.addAttribute(
                "items",
                itemService.getItemsByStatus(status)
        );

    } else {

        model.addAttribute(
                "items",
                itemService.getAllItems()
        );
    }

    return "dashboard";
}

}