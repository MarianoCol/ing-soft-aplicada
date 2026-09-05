package com.example.shoppingcart.service.dto;

import com.example.shoppingcart.domain.enumeration.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AdminOrderSummaryDTO(
    Long id,
    Instant placedDate,
    OrderStatus status,
    BigDecimal totalPrice,
    Long customerId,
    String customerName,
    String customerEmail
) {}
