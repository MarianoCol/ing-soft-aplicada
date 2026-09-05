package com.example.shoppingcart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentCartServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ShoppingCartRepository shoppingCartRepository = mock(ShoppingCartRepository.class);
    private final CartItemRepository cartItemRepository = mock(CartItemRepository.class);

    private CurrentCartService service;

    @BeforeEach
    void setUp() {
        service = new CurrentCartService(
            userRepository,
            customerRepository,
            productRepository,
            shoppingCartRepository,
            cartItemRepository
        );
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCalculateLineAndCartTotalsOnServer() {
        Customer customer = new Customer().id(10L).firstName("User").lastName("Test").email("user@localhost");
        Product product = new Product().id(1L).name("Producto E2E").price(new BigDecimal("19.99")).stock(25);
        ShoppingCart cart = new ShoppingCart()
            .id(20L)
            .customer(customer)
            .placedDate(Instant.now())
            .status(OrderStatus.PENDING)
            .totalPrice(BigDecimal.ZERO);
        AtomicReference<CartItem> savedItem = new AtomicReference<>();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(customerRepository.findOneByUserLogin("user")).thenReturn(Optional.of(customer));
        when(shoppingCartRepository.findFirstByCustomerIdAndStatusOrderByPlacedDateDesc(10L, OrderStatus.PENDING)).thenReturn(
            Optional.of(cart)
        );
        when(cartItemRepository.findOneByCartIdAndProductId(20L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(30L);
            savedItem.set(item);
            return item;
        });
        when(cartItemRepository.findAllByCartIdOrderById(20L)).thenAnswer(invocation -> List.of(savedItem.get()));

        var result = service.setQuantity(1L, 2);

        assertThat(result.totalPrice()).isEqualByComparingTo("39.98");
        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(item.totalPrice()).isEqualByComparingTo("39.98");
        });
    }

    @Test
    void shouldRejectQuantityGreaterThanStock() {
        Product product = new Product().id(1L).name("Producto E2E").price(new BigDecimal("19.99")).stock(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.setQuantity(1L, 2)).isInstanceOf(StockConflictException.class);
    }

    @Test
    void shouldRejectInactiveProduct() {
        Product product = new Product().id(1L).name("Retirado").price(new BigDecimal("19.99")).stock(10).active(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.setQuantity(1L, 1)).isInstanceOf(ProductUnavailableException.class);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void shouldCreateCustomerAndPendingCartForAuthenticatedUser() {
        User user = new User();
        user.setLogin("user");
        user.setEmail("user@example.test");
        user.setFirstName("Test");
        user.setLastName("User");
        AtomicReference<Customer> createdCustomer = new AtomicReference<>();

        when(customerRepository.findOneByUserLogin("user")).thenReturn(Optional.empty());
        when(userRepository.findOneByLogin("user")).thenReturn(Optional.of(user));
        when(customerRepository.findOneByEmailIgnoreCase("user@example.test")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(10L);
            createdCustomer.set(customer);
            return customer;
        });
        when(shoppingCartRepository.findFirstByCustomerIdAndStatusOrderByPlacedDateDesc(10L, OrderStatus.PENDING)).thenReturn(
            Optional.empty()
        );
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> {
            ShoppingCart cart = invocation.getArgument(0);
            cart.setId(20L);
            return cart;
        });
        when(cartItemRepository.findAllByCartIdOrderById(20L)).thenReturn(List.of());

        var result = service.getCurrentCart();

        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(createdCustomer.get().getUser()).isSameAs(user);
        assertThat(createdCustomer.get().getEmail()).isEqualTo("user@example.test");
    }

    @Test
    void shouldRejectCartThatIsNoLongerPending() {
        Customer customer = new Customer().id(10L).firstName("User").lastName("Test").email("user@localhost");
        Product product = new Product().id(1L).name("Producto E2E").price(new BigDecimal("19.99")).stock(25);
        ShoppingCart completed = new ShoppingCart()
            .id(20L)
            .customer(customer)
            .placedDate(Instant.now())
            .status(OrderStatus.COMPLETED)
            .totalPrice(BigDecimal.ZERO);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(customerRepository.findOneByUserLogin("user")).thenReturn(Optional.of(customer));
        when(shoppingCartRepository.findFirstByCustomerIdAndStatusOrderByPlacedDateDesc(10L, OrderStatus.PENDING)).thenReturn(
            Optional.of(completed)
        );

        assertThatThrownBy(() -> service.setQuantity(1L, 1)).isInstanceOf(CartNotEditableException.class);
        verify(cartItemRepository, never()).save(any());
    }
}
