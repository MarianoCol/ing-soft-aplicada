package com.example.shoppingcart.service;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.Customer;
import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.User;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.repository.CustomerRepository;
import com.example.shoppingcart.repository.ProductRepository;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import com.example.shoppingcart.repository.UserRepository;
import com.example.shoppingcart.security.SecurityUtils;
import com.example.shoppingcart.service.dto.CartItemViewDTO;
import com.example.shoppingcart.service.dto.CartViewDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CurrentCartService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;

    public CurrentCartService(
        UserRepository userRepository,
        CustomerRepository customerRepository,
        ProductRepository productRepository,
        ShoppingCartRepository shoppingCartRepository,
        CartItemRepository cartItemRepository
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public CartViewDTO getCurrentCart() {
        Customer customer = getOrCreateCurrentCustomer();
        ShoppingCart cart = getOrCreatePendingCart(customer);
        return toView(cart);
    }

    public CartViewDTO setQuantity(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductUnavailableException(productId);
        }
        if (quantity > product.getStock()) {
            throw new StockConflictException(productId, quantity, product.getStock());
        }

        Customer customer = getOrCreateCurrentCustomer();
        ShoppingCart cart = getOrCreatePendingCart(customer);
        if (cart.getStatus() != OrderStatus.PENDING) {
            throw new CartNotEditableException(cart.getId());
        }
        CartItem item = cartItemRepository.findOneByCartIdAndProductId(cart.getId(), productId).orElseGet(CartItem::new);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(item);

        updateCartTotal(cart);
        return toView(cart);
    }

    public CartViewDTO removeItem(Long productId) {
        Customer customer = getOrCreateCurrentCustomer();
        ShoppingCart cart = getOrCreatePendingCart(customer);
        assertPending(cart);

        cartItemRepository.findOneByCartIdAndProductId(cart.getId(), productId).ifPresent(cartItemRepository::delete);
        updateCartTotal(cart);
        return toView(cart);
    }

    public CartViewDTO checkout() {
        Customer customer = getOrCreateCurrentCustomer();
        ShoppingCart currentCart = getOrCreatePendingCart(customer);
        ShoppingCart cart = shoppingCartRepository
            .findByIdForUpdate(currentCart.getId())
            .orElseThrow(() -> new CartNotEditableException(currentCart.getId()));
        assertPending(cart);
        if (!cart.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("The shopping cart belongs to another customer");
        }

        List<CartItem> items = cartItemRepository.findAllByCartIdOrderById(cart.getId());
        if (items.isEmpty()) {
            throw new EmptyCartException();
        }

        List<Long> productIds = items.stream().map(item -> item.getProduct().getId()).distinct().sorted().toList();
        Map<Long, Product> products = new HashMap<>();
        productRepository.findAllByIdForUpdate(productIds).forEach(product -> products.put(product.getId(), product));

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            Long productId = item.getProduct().getId();
            Product product = products.get(productId);
            if (product == null) {
                throw new ProductNotFoundException(productId);
            }
            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ProductUnavailableException(productId);
            }
            if (item.getQuantity() > product.getStock()) {
                throw new StockConflictException(productId, item.getQuantity(), product.getStock());
            }
            product.setStock(product.getStock() - item.getQuantity());
            total = total.add(item.getTotalPrice());
        }

        productRepository.saveAll(products.values());
        cart.setTotalPrice(total);
        cart.setPlacedDate(Instant.now());
        cart.setStatus(OrderStatus.COMPLETED);
        shoppingCartRepository.save(cart);
        return toView(cart);
    }

    private void assertPending(ShoppingCart cart) {
        if (cart.getStatus() != OrderStatus.PENDING) {
            throw new CartNotEditableException(cart.getId());
        }
    }

    private void updateCartTotal(ShoppingCart cart) {
        BigDecimal total = cartItemRepository
            .findAllByCartIdOrderById(cart.getId())
            .stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
        shoppingCartRepository.save(cart);
    }

    private Customer getOrCreateCurrentCustomer() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccessDeniedException("Authentication is required"));
        return customerRepository.findOneByUserLogin(login).orElseGet(() -> {
            User user = userRepository.findOneByLogin(login).orElseThrow(() -> new AccessDeniedException("Authenticated user no longer exists"));
            String email = user.getEmail() != null ? user.getEmail() : login + "@local.invalid";
            Customer customer = customerRepository.findOneByEmailIgnoreCase(email).orElseGet(Customer::new);
            customer.setFirstName(defaultName(user.getFirstName(), login));
            customer.setLastName(defaultName(user.getLastName(), login));
            customer.setEmail(email.toLowerCase());
            customer.setUser(user);
            return customerRepository.save(customer);
        });
    }

    private String defaultName(String value, String login) {
        return value == null || value.isBlank() ? login : value;
    }

    private ShoppingCart getOrCreatePendingCart(Customer customer) {
        return shoppingCartRepository
            .findFirstByCustomerIdAndStatusOrderByPlacedDateDesc(customer.getId(), OrderStatus.PENDING)
            .orElseGet(() ->
                shoppingCartRepository.save(
                    new ShoppingCart()
                        .customer(customer)
                        .placedDate(Instant.now())
                        .status(OrderStatus.PENDING)
                        .totalPrice(BigDecimal.ZERO)
                )
            );
    }

    private CartViewDTO toView(ShoppingCart cart) {
        List<CartItemViewDTO> items = cartItemRepository
            .findAllByCartIdOrderById(cart.getId())
            .stream()
            .map(item ->
                new CartItemViewDTO(
                    item.getId(),
                    item.getQuantity(),
                    item.getTotalPrice(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getPrice(),
                    item.getProduct().getStock()
                )
            )
            .toList();
        return new CartViewDTO(cart.getId(), cart.getPlacedDate(), cart.getStatus(), cart.getTotalPrice(), items);
    }
}
