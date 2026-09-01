package com.example.shoppingcart.repository;

import com.example.shoppingcart.domain.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Customer entity.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findOneByUserLogin(String login);

    Optional<Customer> findOneByEmailIgnoreCase(String email);

    default Optional<Customer> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Customer> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Customer> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select customer from Customer customer left join fetch customer.user",
        countQuery = "select count(customer) from Customer customer"
    )
    Page<Customer> findAllWithToOneRelationships(Pageable pageable);

    @Query("select customer from Customer customer left join fetch customer.user")
    List<Customer> findAllWithToOneRelationships();

    @Query("select customer from Customer customer left join fetch customer.user where customer.id =:id")
    Optional<Customer> findOneWithToOneRelationships(@Param("id") Long id);
}
