package com.example.shoppingcart.web.rest;

import com.example.shoppingcart.service.CurrentCartService;
import com.example.shoppingcart.service.dto.CartQuantityRequest;
import com.example.shoppingcart.service.dto.CartViewDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CurrentCartResource {

    private final CurrentCartService currentCartService;

    public CurrentCartResource(CurrentCartService currentCartService) {
        this.currentCartService = currentCartService;
    }

    @GetMapping("")
    public ResponseEntity<CartViewDTO> getCurrentCart() {
        return ResponseEntity.ok(currentCartService.getCurrentCart());
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartViewDTO> setQuantity(
        @PathVariable Long productId,
        @Valid @RequestBody CartQuantityRequest request
    ) {
        return ResponseEntity.ok(currentCartService.setQuantity(productId, request.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartViewDTO> removeItem(@PathVariable Long productId) {
        return ResponseEntity.ok(currentCartService.removeItem(productId));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CartViewDTO> checkout() {
        return ResponseEntity.ok(currentCartService.checkout());
    }
}
