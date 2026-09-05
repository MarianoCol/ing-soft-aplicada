package com.example.shoppingcart.web.rest;

import com.example.shoppingcart.service.AdminDashboardService;
import com.example.shoppingcart.service.dto.AdminDashboardDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardResource {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardResource(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("")
    public AdminDashboardDTO get() {
        return adminDashboardService.getSummary();
    }
}
