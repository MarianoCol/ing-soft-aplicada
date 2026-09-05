package com.example.shoppingcart.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "The shopping cart is empty")
public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("The shopping cart must contain at least one product before checkout");
    }
}
