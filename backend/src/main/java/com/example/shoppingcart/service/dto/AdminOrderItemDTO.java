package com.example.shoppingcart.service.dto;

import java.math.BigDecimal;

public record AdminOrderItemDTO(
    Long id,
    Integer quantity,
    BigDecimal totalPrice,
    Long productId,
    String productName,
    BigDecimal unitPrice
) {}
