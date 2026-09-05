package com.example.shoppingcart.service.mapper;

import com.example.shoppingcart.domain.Customer;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.service.dto.CustomerDTO;
import com.example.shoppingcart.service.dto.ShoppingCartDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ShoppingCart} and its DTO {@link ShoppingCartDTO}.
 */
@Mapper(componentModel = "spring")
public interface ShoppingCartMapper extends EntityMapper<ShoppingCartDTO, ShoppingCart> {
    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerEmail")
    ShoppingCartDTO toDto(ShoppingCart s);

    @Named("customerEmail")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    CustomerDTO toDtoCustomerEmail(Customer customer);
}
