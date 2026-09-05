package com.example.shoppingcart.service.mapper;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.service.dto.CartItemDTO;
import com.example.shoppingcart.service.dto.ProductDTO;
import com.example.shoppingcart.service.dto.ShoppingCartDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CartItem} and its DTO {@link CartItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface CartItemMapper extends EntityMapper<CartItemDTO, CartItem> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productName")
    @Mapping(target = "cart", source = "cart", qualifiedByName = "shoppingCartId")
    CartItemDTO toDto(CartItem s);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductDTO toDtoProductName(Product product);

    @Named("shoppingCartId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ShoppingCartDTO toDtoShoppingCartId(ShoppingCart shoppingCart);
}
