package com.example.shoppingcart.service;

import com.example.shoppingcart.domain.Product;
import com.example.shoppingcart.repository.CartItemRepository;
import com.example.shoppingcart.repository.ProductRepository;
import com.example.shoppingcart.service.dto.AdminProductDTO;
import com.example.shoppingcart.service.dto.ProductDTO;
import com.example.shoppingcart.service.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductMapper productMapper;

    public AdminProductService(ProductRepository productRepository, CartItemRepository cartItemRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public Page<AdminProductDTO> findAll(String search, Boolean active, Pageable pageable) {
        String normalizedSearch = search == null || search.isBlank() ? "" : search.trim();
        return productRepository.findForAdmin(normalizedSearch, active, pageable).map(this::toDto);
    }

    public AdminProductDTO create(ProductDTO request) {
        Product product = productMapper.toEntity(request);
        product.setId(null);
        product.setActive(true);
        return toDto(productRepository.save(product));
    }

    public AdminProductDTO update(Long id, ProductDTO request) {
        Product product = getProduct(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return toDto(productRepository.save(product));
    }

    public AdminProductDTO setActive(Long id, boolean active) {
        Product product = getProduct(id);
        product.setActive(active);
        return toDto(productRepository.save(product));
    }

    @Transactional(noRollbackFor = ProductInUseException.class)
    public void delete(Long id) {
        Product product = getProduct(id);
        if (cartItemRepository.existsByProductId(id)) {
            product.setActive(false);
            productRepository.save(product);
            throw new ProductInUseException(id);
        }
        productRepository.delete(product);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private AdminProductDTO toDto(Product product) {
        return new AdminProductDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getActive(),
            !cartItemRepository.existsByProductId(product.getId())
        );
    }
}
