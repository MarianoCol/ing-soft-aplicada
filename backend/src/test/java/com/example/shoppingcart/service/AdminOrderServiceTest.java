package com.example.shoppingcart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.Customer;
import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.repository.ProductRepository;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminOrderServiceTest {

    private final ShoppingCartRepository cartRepository = mock(ShoppingCartRepository.class);
    private final CartItemRepository itemRepository = mock(CartItemRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private AdminOrderService service;

    @BeforeEach
    void setUp() {
        service = new AdminOrderService(cartRepository, itemRepository, productRepository);
    }

    @Test
    void shouldCompletePendingOrderAndDecreaseStock() {
        Customer customer = new Customer().id(1L).firstName("Ada").lastName("Lovelace").email("ada@example.test");
        Product product = new Product().id(2L).name("Teclado").price(new BigDecimal("10.00")).stock(5);
        ShoppingCart cart = new ShoppingCart()
            .id(3L)
            .customer(customer)
            .placedDate(Instant.now())
            .status(OrderStatus.PENDING)
            .totalPrice(new BigDecimal("20.00"));
        CartItem item = new CartItem().id(4L).cart(cart).product(product).quantity(2).totalPrice(new BigDecimal("20.00"));
        when(cartRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(cart));
        when(itemRepository.findAllByCartIdOrderById(3L)).thenReturn(List.of(item));
        when(productRepository.findAllByIdForUpdate(List.of(2L))).thenReturn(List.of(product));

        var result = service.changeStatus(3L, OrderStatus.COMPLETED);

        assertThat(result.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(product.getStock()).isEqualTo(3);
        verify(productRepository).saveAll(any());
    }

    @Test
    void shouldRejectChangesToFinalOrder() {
        ShoppingCart cart = new ShoppingCart().id(3L).status(OrderStatus.COMPLETED);
        when(cartRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> service.changeStatus(3L, OrderStatus.CANCELLED))
            .isInstanceOf(InvalidOrderTransitionException.class);
    }
}
