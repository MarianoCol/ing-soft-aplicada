package com.example.shoppingcart.web.rest;

import com.example.shoppingcart.service.OrderHistoryService;
import com.example.shoppingcart.service.dto.OrderDetailDTO;
import com.example.shoppingcart.service.dto.OrderSummaryDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/member/orders")
public class MemberOrderResource {

    private final OrderHistoryService orderHistoryService;

    public MemberOrderResource(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @GetMapping("")
    public ResponseEntity<List<OrderSummaryDTO>> list(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        Page<OrderSummaryDTO> page = orderHistoryService.findAll(pageable);

        HttpHeaders headers =
            PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
            );

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(orderHistoryService.findOne(id));
    }
}