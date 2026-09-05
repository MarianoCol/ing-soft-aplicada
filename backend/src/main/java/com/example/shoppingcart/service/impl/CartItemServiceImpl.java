package com.example.shoppingcart.service.impl;

import com.example.shoppingcart.domain.CartItem;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.service.CartItemService;
import com.example.shoppingcart.service.dto.CartItemDTO;
import com.example.shoppingcart.service.mapper.CartItemMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.example.shoppingcart.domain.CartItem}.
 */
@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {

    private static final Logger LOG = LoggerFactory.getLogger(CartItemServiceImpl.class);

    private final CartItemRepository cartItemRepository;

    private final CartItemMapper cartItemMapper;

    public CartItemServiceImpl(CartItemRepository cartItemRepository, CartItemMapper cartItemMapper) {
        this.cartItemRepository = cartItemRepository;
        this.cartItemMapper = cartItemMapper;
    }

    @Override
    public CartItemDTO save(CartItemDTO cartItemDTO) {
        LOG.debug("Request to save CartItem : {}", cartItemDTO);
        CartItem cartItem = cartItemMapper.toEntity(cartItemDTO);
        cartItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toDto(cartItem);
    }

    @Override
    public CartItemDTO update(CartItemDTO cartItemDTO) {
        LOG.debug("Request to update CartItem : {}", cartItemDTO);
        CartItem cartItem = cartItemMapper.toEntity(cartItemDTO);
        cartItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toDto(cartItem);
    }

    @Override
    public Optional<CartItemDTO> partialUpdate(CartItemDTO cartItemDTO) {
        LOG.debug("Request to partially update CartItem : {}", cartItemDTO);

        return cartItemRepository
            .findById(cartItemDTO.getId())
            .map(existingCartItem -> {
                cartItemMapper.partialUpdate(existingCartItem, cartItemDTO);

                return existingCartItem;
            })
            .map(cartItemRepository::save)
            .map(cartItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CartItemDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all CartItems");
        return cartItemRepository.findAll(pageable).map(cartItemMapper::toDto);
    }

    public Page<CartItemDTO> findAllWithEagerRelationships(Pageable pageable) {
        return cartItemRepository.findAllWithEagerRelationships(pageable).map(cartItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CartItemDTO> findOne(Long id) {
        LOG.debug("Request to get CartItem : {}", id);
        return cartItemRepository.findOneWithEagerRelationships(id).map(cartItemMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete CartItem : {}", id);
        cartItemRepository.deleteById(id);
    }
}
