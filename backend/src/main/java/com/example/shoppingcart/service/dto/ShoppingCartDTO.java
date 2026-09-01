package com.example.shoppingcart.service.dto;

import com.example.shoppingcart.domain.enumeration.OrderStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.example.shoppingcart.domain.ShoppingCart} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ShoppingCartDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant placedDate;

    @NotNull
    private OrderStatus status;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal totalPrice;

    @NotNull
    private CustomerDTO customer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getPlacedDate() {
        return placedDate;
    }

    public void setPlacedDate(Instant placedDate) {
        this.placedDate = placedDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShoppingCartDTO)) {
            return false;
        }

        ShoppingCartDTO shoppingCartDTO = (ShoppingCartDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, shoppingCartDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ShoppingCartDTO{" +
            "id=" + getId() +
            ", placedDate='" + getPlacedDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", totalPrice=" + getTotalPrice() +
            ", customer=" + getCustomer() +
            "}";
    }
}
