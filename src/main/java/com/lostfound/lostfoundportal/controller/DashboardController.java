package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.service.ItemService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;


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
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            Model model) {

        model.addAttribute(
                "items",
                itemService.searchItems(keyword, location, status, dateFrom, dateTo, sort));

        // Powers the location filter dropdown - always the current full list
        // of locations in use, regardless of what's currently filtered.
        model.addAttribute("locations", itemService.getDistinctLocations());

        // Echoed back so the form/dropdowns/active filter button reflect
        // whatever is currently applied, instead of resetting on every search.
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        model.addAttribute("selectedSort", sort);

        return "dashboard";
    }

}