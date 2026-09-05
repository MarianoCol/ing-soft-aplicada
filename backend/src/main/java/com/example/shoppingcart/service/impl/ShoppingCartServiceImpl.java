package com.example.shoppingcart.service.impl;

import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import com.example.shoppingcart.service.ShoppingCartService;
import com.example.shoppingcart.service.dto.ShoppingCartDTO;
import com.example.shoppingcart.service.mapper.ShoppingCartMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.example.shoppingcart.domain.ShoppingCart}.
 */
@Service
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

    private final ShoppingCartRepository shoppingCartRepository;

    private final ShoppingCartMapper shoppingCartMapper;

    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository, ShoppingCartMapper shoppingCartMapper) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.shoppingCartMapper = shoppingCartMapper;
    }

    @Override
    public ShoppingCartDTO save(ShoppingCartDTO shoppingCartDTO) {
        LOG.debug("Request to save ShoppingCart : {}", shoppingCartDTO);
        ShoppingCart shoppingCart = shoppingCartMapper.toEntity(shoppingCartDTO);
        shoppingCart = shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartDTO update(ShoppingCartDTO shoppingCartDTO) {
        LOG.debug("Request to update ShoppingCart : {}", shoppingCartDTO);
        ShoppingCart shoppingCart = shoppingCartMapper.toEntity(shoppingCartDTO);
        shoppingCart = shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public Optional<ShoppingCartDTO> partialUpdate(ShoppingCartDTO shoppingCartDTO) {
        LOG.debug("Request to partially update ShoppingCart : {}", shoppingCartDTO);

        return shoppingCartRepository
            .findById(shoppingCartDTO.getId())
            .map(existingShoppingCart -> {
                shoppingCartMapper.partialUpdate(existingShoppingCart, shoppingCartDTO);

                return existingShoppingCart;
            })
            .map(shoppingCartRepository::save)
            .map(shoppingCartMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShoppingCartDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ShoppingCarts");
        return shoppingCartRepository.findAll(pageable).map(shoppingCartMapper::toDto);
    }

    public Page<ShoppingCartDTO> findAllWithEagerRelationships(Pageable pageable) {
        return shoppingCartRepository.findAllWithEagerRelationships(pageable).map(shoppingCartMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCartDTO> findOne(Long id) {
        LOG.debug("Request to get ShoppingCart : {}", id);
        return shoppingCartRepository.findOneWithEagerRelationships(id).map(shoppingCartMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete ShoppingCart : {}", id);
        shoppingCartRepository.deleteById(id);
    }
}
