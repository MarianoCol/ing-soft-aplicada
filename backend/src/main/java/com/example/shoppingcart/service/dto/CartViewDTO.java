package com.example.shoppingcart.service.dto;

import com.example.shoppingcart.domain.enumeration.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartViewDTO(Long id, Instant placedDate, OrderStatus status, BigDecimal totalPrice, List<CartItemViewDTO> items) {}
