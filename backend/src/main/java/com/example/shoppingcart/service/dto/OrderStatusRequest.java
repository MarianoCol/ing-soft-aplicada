package com.example.shoppingcart.service.dto;

import com.example.shoppingcart.domain.enumeration.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull OrderStatus status) {}
