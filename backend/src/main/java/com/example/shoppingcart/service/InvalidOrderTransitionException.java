package com.example.shoppingcart.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Invalid order status transition")
public class InvalidOrderTransitionException extends RuntimeException {
    public InvalidOrderTransitionException(Long orderId) {
        super("Order " + orderId + " is not pending or the requested status is invalid");
    }
}
