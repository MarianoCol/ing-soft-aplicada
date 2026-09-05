package com.example.shoppingcart.service.dto;

public record AdminDashboardDTO(
    long activeProducts,
    long inactiveProducts,
    long pendingOrders,
    long completedOrders,
    long cancelledOrders,
    long activeUsers
) {}
