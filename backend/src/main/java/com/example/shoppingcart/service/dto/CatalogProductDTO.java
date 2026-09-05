package com.example.shoppingcart.service.dto;

import java.io.Serializable;

public record CatalogProductDTO(Long id, String name, String description, Integer stock) implements Serializable {
    public static CatalogProductDTO from(ProductDTO product) {
        return new CatalogProductDTO(product.getId(), product.getName(), product.getDescription(), product.getStock());
    }
}
