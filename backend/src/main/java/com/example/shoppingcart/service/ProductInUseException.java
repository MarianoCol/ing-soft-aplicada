package com.example.shoppingcart.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Product is used by a cart")
public class ProductInUseException extends RuntimeException {
    public ProductInUseException(Long productId) {
        super("Product " + productId + " is used by a cart and cannot be deleted");
    }
}
