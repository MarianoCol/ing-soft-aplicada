package com.example.shoppingcart.service.mapper;

import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.service.dto.ProductDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Product} and its DTO {@link ProductDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper extends EntityMapper<ProductDTO, Product> {}
