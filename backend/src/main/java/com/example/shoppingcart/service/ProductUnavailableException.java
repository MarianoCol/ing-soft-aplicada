package com.example.shoppingcart.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Product is inactive")
public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(Long productId) {
        super("Product " + productId + " is inactive");
    }
}
