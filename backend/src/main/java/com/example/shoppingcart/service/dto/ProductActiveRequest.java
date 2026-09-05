package com.example.shoppingcart.service.dto;

import jakarta.validation.constraints.NotNull;

public record ProductActiveRequest(@NotNull Boolean active) {}
