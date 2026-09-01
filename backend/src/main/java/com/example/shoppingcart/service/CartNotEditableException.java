package com.example.shoppingcart.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CartNotEditableException extends RuntimeException {

    public CartNotEditableException(Long cartId) {
        super("Shopping cart " + cartId + " is not pending and cannot be edited");
    }
}
