package com.example.shoppingcart.service;

import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.repository.ProductRepository;
import com.example.shoppingcart.repository.ShoppingCartRepository;
import com.example.shoppingcart.repository.UserRepository;
import com.example.shoppingcart.service.dto.AdminDashboardDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;

    public AdminDashboardService(
        ProductRepository productRepository,
        ShoppingCartRepository shoppingCartRepository,
        UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.shoppingCartRepository = shoppingCartRepository;
        this.userRepository = userRepository;
    }

    public AdminDashboardDTO getSummary() {
        return new AdminDashboardDTO(
            productRepository.countByActiveTrue(),
            productRepository.countByActiveFalse(),
            shoppingCartRepository.countByStatus(OrderStatus.PENDING),
            shoppingCartRepository.countByStatus(OrderStatus.COMPLETED),
            shoppingCartRepository.countByStatus(OrderStatus.CANCELLED),
            userRepository.countByActivatedTrue()
        );
    }
}
