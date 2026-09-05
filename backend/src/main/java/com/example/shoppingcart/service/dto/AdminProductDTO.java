package com.example.shoppingcart.service.dto;

import java.math.BigDecimal;

public record AdminProductDTO(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    Boolean active,
    boolean deletable
) {}
