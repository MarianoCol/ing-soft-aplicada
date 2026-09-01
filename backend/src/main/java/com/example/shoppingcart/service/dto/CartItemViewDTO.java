package com.example.shoppingcart.service.dto;

import java.math.BigDecimal;

public record CartItemViewDTO(
    Long id,
    Integer quantity,
    BigDecimal totalPrice,
    Long productId,
    String productName,
    BigDecimal unitPrice,
    Integer stock
) {}
