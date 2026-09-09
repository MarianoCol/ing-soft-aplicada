package com.example.shoppingcart.web.rest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shoppingcart.IntegrationTest;
import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.domain.Customer;
import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.User;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class MemberOrderResourceIT {

    private static final String API_URL = "/api/member/orders";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    private ShoppingCart completedOrder;
    private ShoppingCart cancelledOrder;
    private ShoppingCart pendingOrder;
    private ShoppingCart otherUsersOrder;

    @BeforeEach
    void setUp() {
        User currentUser = userRepository
            .findOneByLogin("user")
            .orElseThrow();

        User otherUser = userRepository
            .findOneByLogin("admin")
            .orElseThrow();

        Customer currentCustomer = new Customer()
            .firstName("History")
            .lastName("User")
            .email("history-user@example.test")
            .user(currentUser);

        Customer otherCustomer = new Customer()
            .firstName("Other")
            .lastName("User")
            .email("history-other@example.test")
            .user(otherUser);

        em.persist(currentCustomer);
        em.persist(otherCustomer);

        Product product = new Product()
            .name("Teclado histórico")
            .description("Producto utilizado por el test")
            .price(new BigDecimal("15.00"))
            .stock(10)
            .active(true);

        em.persist(product);

        completedOrder = persistOrder(
            currentCustomer,
            OrderStatus.COMPLETED,
            "24.00",
            "2026-09-08T12:00:00Z"
        );

        cancelledOrder = persistOrder(
            currentCustomer,
            OrderStatus.CANCELLED,
            "18.00",
            "2026-09-07T12:00:00Z"
        );

        pendingOrder = persistOrder(
            currentCustomer,
            OrderStatus.PENDING,
            "10.00",
            "2026-09-09T12:00:00Z"
        );

        otherUsersOrder = persistOrder(
            otherCustomer,
            OrderStatus.COMPLETED,
            "99.00",
            "2026-09-06T12:00:00Z"
        );

        CartItem item = new CartItem()
            .quantity(2)
            .totalPrice(new BigDecimal("24.00"))
            .product(product)
            .cart(completedOrder);

        em.persist(item);
        em.flush();
        em.clear();
    }

    @Test
    @WithMockUser(username = "user")
    void shouldListOnlyCurrentUsersFinishedOrders() throws Exception {
        mockMvc
            .perform(
                get(API_URL)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "placedDate,desc")
            )
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(
                jsonPath(
                    "$[*].id",
                    containsInAnyOrder(
                        completedOrder.getId().intValue(),
                        cancelledOrder.getId().intValue()
                    )
                )
            );
    }

    @Test
    @WithMockUser(username = "user")
    void shouldReturnCurrentUsersOrderDetail() throws Exception {
        mockMvc
            .perform(get(API_URL + "/{id}", completedOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(completedOrder.getId()))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.totalPrice").value(24.00))
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].productName").value("Teclado histórico"))
            .andExpect(jsonPath("$.items[0].quantity").value(2))
            .andExpect(jsonPath("$.items[0].unitPrice").value(12.00));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldNotExposeOtherUsersOrder() throws Exception {
        mockMvc
            .perform(get(API_URL + "/{id}", otherUsersOrder.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldNotExposePendingCartAsHistory() throws Exception {
        mockMvc
            .perform(get(API_URL + "/{id}", pendingOrder.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithUnauthenticatedMockUser
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc
            .perform(get(API_URL))
            .andExpect(status().isUnauthorized());
    }

    private ShoppingCart persistOrder(
        Customer customer,
        OrderStatus status,
        String totalPrice,
        String placedDate
    ) {
        ShoppingCart order = new ShoppingCart()
            .customer(customer)
            .status(status)
            .totalPrice(new BigDecimal(totalPrice))
            .placedDate(Instant.parse(placedDate));

        em.persist(order);
        return order;
    }
}