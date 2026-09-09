package com.example.shoppingcart.service.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
    Long id,
    Integer quantity,
    BigDecimal totalPrice,
    Long productId,
    String productName,
    BigDecimal unitPrice
) {}