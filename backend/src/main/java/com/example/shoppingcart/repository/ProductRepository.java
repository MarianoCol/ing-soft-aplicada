package com.example.shoppingcart.repository;

import com.example.shoppingcart.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Product entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByActiveTrue(Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(Long id);

    long countByActiveTrue();

    long countByActiveFalse();

    @Query(
        "select p from Product p where (:active is null or p.active = :active) and " +
        "(:search is null or lower(p.name) like lower(concat('%', :search, '%')) or lower(coalesce(p.description, '')) like lower(concat('%', :search, '%')))"
    )
    Page<Product> findForAdmin(@Param("search") String search, @Param("active") Boolean active, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
