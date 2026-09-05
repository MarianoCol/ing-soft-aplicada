package com.example.shoppingcart.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Insufficient stock")
public class StockConflictException extends RuntimeException {

    public StockConflictException(Long productId, int requested, int available) {
        super("Product " + productId + " has " + available + " units available; " + requested + " were requested");
    }
}
