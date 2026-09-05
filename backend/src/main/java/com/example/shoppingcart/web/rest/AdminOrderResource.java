package com.example.shoppingcart.web.rest;

import com.example.shoppingcart.domain.enumeration.OrderStatus;
import com.example.shoppingcart.service.AdminOrderService;
import com.example.shoppingcart.service.dto.AdminOrderDetailDTO;
import com.example.shoppingcart.service.dto.AdminOrderSummaryDTO;
import com.example.shoppingcart.service.dto.OrderStatusRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderResource {

    private final AdminOrderService adminOrderService;

    public AdminOrderResource(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping("")
    public ResponseEntity<List<AdminOrderSummaryDTO>> list(
        @RequestParam(required = false) OrderStatus status,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        Page<AdminOrderSummaryDTO> page = adminOrderService.findAll(status, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderDetailDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.findOne(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AdminOrderDetailDTO> changeStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(adminOrderService.changeStatus(id, request.status()));
    }
}
