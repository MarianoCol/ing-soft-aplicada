package com.example.shoppingcart.service;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.repository.ProductRepository;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import com.example.shoppingcart.service.dto.AdminOrderDetailDTO;
import com.example.shoppingcart.service.dto.AdminOrderItemDTO;
import com.example.shoppingcart.service.dto.AdminOrderSummaryDTO;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminOrderService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public AdminOrderService(
        ShoppingCartRepository shoppingCartRepository,
        CartItemRepository cartItemRepository,
        ProductRepository productRepository
    ) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderSummaryDTO> findAll(OrderStatus status, Pageable pageable) {
        return shoppingCartRepository.findForAdmin(status, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailDTO findOne(Long id) {
        ShoppingCart cart = shoppingCartRepository.findOneWithEagerRelationships(id).orElseThrow(() -> orderNotFound(id));
        return toDetail(cart);
    }

    public AdminOrderDetailDTO changeStatus(Long id, OrderStatus requestedStatus) {
        ShoppingCart cart = shoppingCartRepository.findByIdForUpdate(id).orElseThrow(() -> orderNotFound(id));
        if (cart.getStatus() != OrderStatus.PENDING || (requestedStatus != OrderStatus.COMPLETED && requestedStatus != OrderStatus.CANCELLED)) {
            throw new InvalidOrderTransitionException(id);
        }

        List<CartItem> items = cartItemRepository.findAllByCartIdOrderById(id);
        if (requestedStatus == OrderStatus.COMPLETED) {
            complete(items);
        }
        cart.setStatus(requestedStatus);
        shoppingCartRepository.save(cart);
        return toDetail(cart, items);
    }

    private void complete(List<CartItem> items) {
        if (items.isEmpty()) {
            return;
        }
        List<Long> ids = items.stream().map(item -> item.getProduct().getId()).distinct().sorted().toList();
        Map<Long, Product> products = productRepository
            .findAllByIdForUpdate(ids)
            .stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (CartItem item : items) {
            Product product = products.get(item.getProduct().getId());
            if (product == null || !Boolean.TRUE.equals(product.getActive())) {
                throw new ProductUnavailableException(item.getProduct().getId());
            }
            if (item.getQuantity() > product.getStock()) {
                throw new StockConflictException(product.getId(), item.getQuantity(), product.getStock());
            }
            product.setStock(product.getStock() - item.getQuantity());
        }
        productRepository.saveAll(products.values());
    }

    private AdminOrderSummaryDTO toSummary(ShoppingCart cart) {
        String name = cart.getCustomer().getFirstName() + " " + cart.getCustomer().getLastName();
        return new AdminOrderSummaryDTO(
            cart.getId(),
            cart.getPlacedDate(),
            cart.getStatus(),
            cart.getTotalPrice(),
            cart.getCustomer().getId(),
            name.trim(),
            cart.getCustomer().getEmail()
        );
    }

    private AdminOrderDetailDTO toDetail(ShoppingCart cart) {
        return toDetail(cart, cartItemRepository.findAllByCartIdOrderById(cart.getId()));
    }

    private AdminOrderDetailDTO toDetail(ShoppingCart cart, List<CartItem> items) {
        AdminOrderSummaryDTO summary = toSummary(cart);
        List<AdminOrderItemDTO> itemDtos = items
            .stream()
            .map(item ->
                new AdminOrderItemDTO(
                    item.getId(),
                    item.getQuantity(),
                    item.getTotalPrice(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getTotalPrice().divide(java.math.BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP)
                )
            )
            .toList();
        return new AdminOrderDetailDTO(
            summary.id(),
            summary.placedDate(),
            summary.status(),
            summary.totalPrice(),
            summary.customerId(),
            summary.customerName(),
            summary.customerEmail(),
            itemDtos
        );
    }

    private OrderNotFoundException orderNotFound(Long id) {
        return new OrderNotFoundException(id);
    }
}
