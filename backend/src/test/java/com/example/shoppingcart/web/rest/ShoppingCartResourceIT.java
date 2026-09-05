package com.example.shoppingcart.web.rest;

import static com.example.shoppingcart.domain.ShoppingCartAsserts.*;
import static com.example.shoppingcart.web.rest.TestUtil.createUpdateProxyForBean;
import static com.example.shoppingcart.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.shoppingcart.IntegrationTest;
import com.example.shoppingcart.domain.Customer;
import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import com.example.shoppingcart.security.AuthoritiesConstants;
import com.example.shoppingcart.service.ShoppingCartService;
import com.example.shoppingcart.service.dto.ShoppingCartDTO;
import com.example.shoppingcart.service.mapper.ShoppingCartMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ShoppingCartResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ShoppingCartResourceIT {

    private static final Instant DEFAULT_PLACED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PLACED_DATE = Instant.ofEpochMilli(1702998242553L);

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.PENDING;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.COMPLETED;

    private static final BigDecimal DEFAULT_TOTAL_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_TOTAL_PRICE = new BigDecimal(1);

    private static final String ENTITY_API_URL = "/api/shopping-carts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepositoryMock;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private ShoppingCartService shoppingCartServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restShoppingCartMockMvc;

    private ShoppingCart shoppingCart;

    private ShoppingCart insertedShoppingCart;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShoppingCart createEntity(EntityManager em) {
        ShoppingCart shoppingCart = new ShoppingCart()
            .placedDate(DEFAULT_PLACED_DATE)
            .status(DEFAULT_STATUS)
            .totalPrice(DEFAULT_TOTAL_PRICE);
        // Add required entity
        Customer customer;
        if (TestUtil.findAll(em, Customer.class).isEmpty()) {
            customer = CustomerResourceIT.createEntity();
            em.persist(customer);
            em.flush();
        } else {
            customer = TestUtil.findAll(em, Customer.class).get(0);
        }
        shoppingCart.setCustomer(customer);
        return shoppingCart;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShoppingCart createUpdatedEntity(EntityManager em) {
        ShoppingCart updatedShoppingCart = new ShoppingCart()
            .placedDate(UPDATED_PLACED_DATE)
            .status(UPDATED_STATUS)
            .totalPrice(UPDATED_TOTAL_PRICE);
        // Add required entity
        Customer customer;
        if (TestUtil.findAll(em, Customer.class).isEmpty()) {
            customer = CustomerResourceIT.createUpdatedEntity();
            em.persist(customer);
            em.flush();
        } else {
            customer = TestUtil.findAll(em, Customer.class).get(0);
        }
        updatedShoppingCart.setCustomer(customer);
        return updatedShoppingCart;
    }

    @BeforeEach
    void initTest() {
        shoppingCart = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedShoppingCart != null) {
            shoppingCartRepository.delete(insertedShoppingCart);
            insertedShoppingCart = null;
        }
    }

    @Test
    @Transactional
    void createShoppingCart() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ShoppingCart
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);
        var returnedShoppingCartDTO = om.readValue(
            restShoppingCartMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shoppingCartDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ShoppingCartDTO.class
        );

        // Validate the ShoppingCart in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedShoppingCart = shoppingCartMapper.toEntity(returnedShoppingCartDTO);
        assertShoppingCartUpdatableFieldsEquals(returnedShoppingCart, getPersistedShoppingCart(returnedShoppingCart));

        insertedShoppingCart = returnedShoppingCart;
    }

    @Test
    @Transactional
    void createShoppingCartWithExistingId() throws Exception {
        // Create the ShoppingCart with an existing ID
        shoppingCart.setId(1L);
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restShoppingCartMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shoppingCartDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPlacedDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        shoppingCart.setPlacedDate(null);

        // Create the ShoppingCart, which fails.
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        restShoppingCartMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shoppingCartDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        shoppingCart.setStatus(null);

        // Create the ShoppingCart, which fails.
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        restShoppingCartMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shoppingCartDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        shoppingCart.setTotalPrice(null);

        // Create the ShoppingCart, which fails.
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        restShoppingCartMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shoppingCartDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllShoppingCarts() throws Exception {
        // Initialize the database
        insertedShoppingCart = shoppingCartRepository.saveAndFlush(shoppingCart);

        // Get all the shoppingCartList
        restShoppingCartMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(shoppingCart.getId().intValue())))
            .andExpect(jsonPath("$.[*].placedDate").value(hasItem(DEFAULT_PLACED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].totalPrice").value(hasItem(sameNumber(DEFAULT_TOTAL_PRICE))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllShoppingCartsWithEagerRelationshipsIsEnabled() throws Exception {
        when(shoppingCartServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restShoppingCartMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(shoppingCartServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllShoppingCartsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(shoppingCartServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restShoppingCartMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(shoppingCartRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getShoppingCart() throws Exception {
        // Initialize the database
        insertedShoppingCart = shoppingCartRepository.saveAndFlush(shoppingCart);

        // Get the shoppingCart
        restShoppingCartMockMvc
            .perform(get(ENTITY_API_URL_ID, shoppingCart.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(shoppingCart.getId().intValue()))
            .andExpect(jsonPath("$.placedDate").value(DEFAULT_PLACED_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.totalPrice").value(sameNumber(DEFAULT_TOTAL_PRICE)));
    }

    @Test
    @Transactional
    void getNonExistingShoppingCart() throws Exception {
        // Get the shoppingCart
        restShoppingCartMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingShoppingCart() throws Exception {
        // Initialize the database
        insertedShoppingCart = shoppingCartRepository.saveAndFlush(shoppingCart);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shoppingCart
        ShoppingCart updatedShoppingCart = shoppingCartRepository.findById(shoppingCart.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedShoppingCart are not directly saved in db
        em.detach(updatedShoppingCart);
        updatedShoppingCart.placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).totalPrice(UPDATED_TOTAL_PRICE);
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(updatedShoppingCart);

        restShoppingCartMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shoppingCartDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shoppingCartDTO))
            )
            .andExpect(status().isOk());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedShoppingCartToMatchAllProperties(updatedShoppingCart);
    }

    @Test
    @Transactional
    void putNonExistingShoppingCart() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shoppingCart.setId(longCount.incrementAndGet());

        // Create the ShoppingCart
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShoppingCartMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shoppingCartDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shoppingCartDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchShoppingCart() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shoppingCart.setId(longCount.incrementAndGet());

        // Create the ShoppingCart
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShoppingCartMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shoppingCartDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamShoppingCart() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shoppingCart.setId(longCount.incrementAndGet());

        // Create the ShoppingCart
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShoppingCartMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shoppingCartDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateShoppingCartWithPatch() throws Exception {
        // Initialize the database
        insertedShoppingCart = shoppingCartRepository.saveAndFlush(shoppingCart);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shoppingCart using partial update
        ShoppingCart partialUpdatedShoppingCart = new ShoppingCart();
        partialUpdatedShoppingCart.setId(shoppingCart.getId());

        partialUpdatedShoppingCart.totalPrice(UPDATED_TOTAL_PRICE);

        restShoppingCartMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShoppingCart.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShoppingCart))
            )
            .andExpect(status().isOk());

        // Validate the ShoppingCart in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShoppingCartUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedShoppingCart, shoppingCart),
            getPersistedShoppingCart(shoppingCart)
        );
    }

    @Test
    @Transactional
    void fullUpdateShoppingCartWithPatch() throws Exception {
        // Initialize the database
        insertedShoppingCart = shoppingCartRepository.saveAndFlush(shoppingCart);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the shoppingCart using partial update
        ShoppingCart partialUpdatedShoppingCart = new ShoppingCart();
        partialUpdatedShoppingCart.setId(shoppingCart.getId());

        partialUpdatedShoppingCart.placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).totalPrice(UPDATED_TOTAL_PRICE);

        restShoppingCartMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShoppingCart.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShoppingCart))
            )
            .andExpect(status().isOk());

        // Validate the ShoppingCart in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShoppingCartUpdatableFieldsEquals(partialUpdatedShoppingCart, getPersistedShoppingCart(partialUpdatedShoppingCart));
    }

    @Test
    @Transactional
    void patchNonExistingShoppingCart() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shoppingCart.setId(longCount.incrementAndGet());

        // Create the ShoppingCart
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShoppingCartMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, shoppingCartDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shoppingCartDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchShoppingCart() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shoppingCart.setId(longCount.incrementAndGet());

        // Create the ShoppingCart
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShoppingCartMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shoppingCartDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamShoppingCart() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        shoppingCart.setId(longCount.incrementAndGet());

        // Create the ShoppingCart
        ShoppingCartDTO shoppingCartDTO = shoppingCartMapper.toDto(shoppingCart);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShoppingCartMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(shoppingCartDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShoppingCart in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteShoppingCart() throws Exception {
        // Initialize the database
        insertedShoppingCart = shoppingCartRepository.saveAndFlush(shoppingCart);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the shoppingCart
        restShoppingCartMockMvc
            .perform(delete(ENTITY_API_URL_ID, shoppingCart.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return shoppingCartRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ShoppingCart getPersistedShoppingCart(ShoppingCart shoppingCart) {
        return shoppingCartRepository.findById(shoppingCart.getId()).orElseThrow();
    }

    protected void assertPersistedShoppingCartToMatchAllProperties(ShoppingCart expectedShoppingCart) {
        assertShoppingCartAllPropertiesEquals(expectedShoppingCart, getPersistedShoppingCart(expectedShoppingCart));
    }

    protected void assertPersistedShoppingCartToMatchUpdatableProperties(ShoppingCart expectedShoppingCart) {
        assertShoppingCartAllUpdatablePropertiesEquals(expectedShoppingCart, getPersistedShoppingCart(expectedShoppingCart));
    }
}
