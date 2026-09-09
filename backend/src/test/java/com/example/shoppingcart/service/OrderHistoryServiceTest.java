package com.example.shoppingcart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class OrderHistoryServiceTest {

    private ShoppingCartRepository shoppingCartRepository;
    private CartItemRepository cartItemRepository;
    private OrderHistoryService service;

    @BeforeEach
    void setUp() {
        shoppingCartRepository = mock(ShoppingCartRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        service = new OrderHistoryService(
            shoppingCartRepository,
            cartItemRepository
        );

        SecurityContextHolder
            .getContext()
            .setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password")
            );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListHistoryForCurrentUser() {
        Pageable pageable = PageRequest.of(0, 10);
        ShoppingCart cart = completedCart();

        when(
            shoppingCartRepository.findHistoryByLogin(
                "user",
                OrderStatus.PENDING,
                pageable
            )
        ).thenReturn(new PageImpl<>(List.of(cart), pageable, 1));

        var result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
            .singleElement()
            .satisfies(order -> {
                assertThat(order.id()).isEqualTo(20L);
                assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);
                assertThat(order.totalPrice())
                    .isEqualByComparingTo("24.00");
            });

        verify(shoppingCartRepository)
            .findHistoryByLogin(
                "user",
                OrderStatus.PENDING,
                pageable
            );
    }

    @Test
    void shouldReturnOrderDetailWithHistoricalUnitPrice() {
        ShoppingCart cart = completedCart();
        Product product = new Product()
            .id(5L)
            .name("Teclado")
            .price(new BigDecimal("15.00"))
            .stock(10);

        CartItem item = new CartItem()
            .id(30L)
            .cart(cart)
            .product(product)
            .quantity(2)
            .totalPrice(new BigDecimal("24.00"));

        when(
            shoppingCartRepository.findHistoryOrderByIdAndLogin(
                20L,
                "user",
                OrderStatus.PENDING
            )
        ).thenReturn(Optional.of(cart));

        when(cartItemRepository.findAllByCartIdOrderById(20L))
            .thenReturn(List.of(item));

        var result = service.findOne(20L);

        assertThat(result.id()).isEqualTo(20L);
        assertThat(result.items())
            .singleElement()
            .satisfies(orderItem -> {
                assertThat(orderItem.productName()).isEqualTo("Teclado");
                assertThat(orderItem.quantity()).isEqualTo(2);
                assertThat(orderItem.unitPrice())
                    .isEqualByComparingTo("12.00");
            });
    }

    @Test
    void shouldNotExposeAnOrderOutsideCurrentUserHistory() {
        when(
            shoppingCartRepository.findHistoryOrderByIdAndLogin(
                99L,
                "user",
                OrderStatus.PENDING
            )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOne(99L))
            .isInstanceOf(OrderNotFoundException.class);

        verify(cartItemRepository, never())
            .findAllByCartIdOrderById(99L);
    }

    @Test
    void shouldRequireAuthentication() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> service.findAll(Pageable.unpaged()))
            .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(
            shoppingCartRepository,
            cartItemRepository
        );
    }

    private ShoppingCart completedCart() {
        return new ShoppingCart()
            .id(20L)
            .placedDate(Instant.parse("2026-09-08T12:00:00Z"))
            .status(OrderStatus.COMPLETED)
            .totalPrice(new BigDecimal("24.00"));
    }
}