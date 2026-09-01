package com.example.shoppingcart.service;

import com.example.shoppingcart.service.dto.ShoppingCartDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.example.shoppingcart.domain.ShoppingCart}.
 */
public interface ShoppingCartService {
    /**
     * Save a shoppingCart.
     *
     * @param shoppingCartDTO the entity to save.
     * @return the persisted entity.
     */
    ShoppingCartDTO save(ShoppingCartDTO shoppingCartDTO);

    /**
     * Updates a shoppingCart.
     *
     * @param shoppingCartDTO the entity to update.
     * @return the persisted entity.
     */
    ShoppingCartDTO update(ShoppingCartDTO shoppingCartDTO);

    /**
     * Partially updates a shoppingCart.
     *
     * @param shoppingCartDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ShoppingCartDTO> partialUpdate(ShoppingCartDTO shoppingCartDTO);

    /**
     * Get all the shoppingCarts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ShoppingCartDTO> findAll(Pageable pageable);

    /**
     * Get all the shoppingCarts with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ShoppingCartDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" shoppingCart.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ShoppingCartDTO> findOne(Long id);

    /**
     * Delete the "id" shoppingCart.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
