package com.example.shoppingcart.repository;

import com.example.shoppingcart.domain.ShoppingCart;
import com.example.shoppingcart.domain.enumeration.OrderStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ShoppingCart entity.
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findFirstByCustomerIdAndStatusOrderByPlacedDateDesc(Long customerId, OrderStatus status);

    long countByStatus(OrderStatus status);

    @Query(
        value = "select c from ShoppingCart c left join fetch c.customer where (:status is null or c.status = :status)",
        countQuery = "select count(c) from ShoppingCart c where (:status is null or c.status = :status)"
    )
    Page<ShoppingCart> findForAdmin(@Param("status") OrderStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ShoppingCart c left join fetch c.customer where c.id = :id")
    Optional<ShoppingCart> findByIdForUpdate(@Param("id") Long id);

    default Optional<ShoppingCart> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ShoppingCart> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ShoppingCart> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select shoppingCart from ShoppingCart shoppingCart left join fetch shoppingCart.customer",
        countQuery = "select count(shoppingCart) from ShoppingCart shoppingCart"
    )
    Page<ShoppingCart> findAllWithToOneRelationships(Pageable pageable);

    @Query("select shoppingCart from ShoppingCart shoppingCart left join fetch shoppingCart.customer")
    List<ShoppingCart> findAllWithToOneRelationships();

    @Query("select shoppingCart from ShoppingCart shoppingCart left join fetch shoppingCart.customer where shoppingCart.id =:id")
    Optional<ShoppingCart> findOneWithToOneRelationships(@Param("id") Long id);
}
