package com.example.shoppingcart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.repository.ProductRepository;
import com.example.shoppingcart.service.mapper.ProductMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminProductServiceTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final CartItemRepository cartItemRepository = mock(CartItemRepository.class);
    private final ProductMapper productMapper = mock(ProductMapper.class);
    private AdminProductService service;

    @BeforeEach
    void setUp() {
        service = new AdminProductService(productRepository, cartItemRepository, productMapper);
    }

    @Test
    void shouldDeactivateButNotDeleteAProductWithHistory() {
        Product product = new Product().id(7L).name("Histórico").price(BigDecimal.TEN).stock(2).active(true);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(cartItemRepository.existsByProductId(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(7L)).isInstanceOf(ProductInUseException.class);

        assertThat(product.getActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void shouldDeleteUnusedProduct() {
        Product product = new Product().id(8L).name("Nuevo").price(BigDecimal.ONE).stock(1);
        when(productRepository.findById(8L)).thenReturn(Optional.of(product));
        when(cartItemRepository.existsByProductId(8L)).thenReturn(false);

        service.delete(8L);

        verify(productRepository).delete(product);
    }
}
