package com.example.shoppingcart.service;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import com.example.shoppingcart.security.SecurityUtils;
import com.example.shoppingcart.service.dto.OrderDetailDTO;
import com.example.shoppingcart.service.dto.OrderItemDTO;
import com.example.shoppingcart.service.dto.OrderSummaryDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderHistoryService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderHistoryService(
        ShoppingCartRepository shoppingCartRepository,
        CartItemRepository cartItemRepository
    ) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Page<OrderSummaryDTO> findAll(Pageable pageable) {
        String login = getCurrentLogin();

        return shoppingCartRepository
            .findHistoryByLogin(login, OrderStatus.PENDING, pageable)
            .map(this::toSummary);
    }

    public OrderDetailDTO findOne(Long id) {
        String login = getCurrentLogin();

        ShoppingCart cart = shoppingCartRepository
            .findHistoryOrderByIdAndLogin(id, login, OrderStatus.PENDING)
            .orElseThrow(() -> new OrderNotFoundException(id));

        List<OrderItemDTO> items = cartItemRepository
            .findAllByCartIdOrderById(id)
            .stream()
            .map(this::toItem)
            .toList();

        return new OrderDetailDTO(
            cart.getId(),
            cart.getPlacedDate(),
            cart.getStatus(),
            cart.getTotalPrice(),
            items
        );
    }

    private OrderSummaryDTO toSummary(ShoppingCart cart) {
        return new OrderSummaryDTO(
            cart.getId(),
            cart.getPlacedDate(),
            cart.getStatus(),
            cart.getTotalPrice()
        );
    }

    private OrderItemDTO toItem(CartItem item) {
        BigDecimal unitPrice = item
            .getTotalPrice()
            .divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP);

        return new OrderItemDTO(
            item.getId(),
            item.getQuantity(),
            item.getTotalPrice(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            unitPrice
        );
    }

    private String getCurrentLogin() {
        return SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new AccessDeniedException("Authentication is required"));
    }
}