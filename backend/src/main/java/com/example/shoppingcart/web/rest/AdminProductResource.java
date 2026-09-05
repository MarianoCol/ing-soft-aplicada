package com.example.shoppingcart.web.rest;

import com.example.shoppingcart.service.AdminProductService;
import com.example.shoppingcart.service.dto.AdminProductDTO;
import com.example.shoppingcart.service.dto.ProductActiveRequest;
import com.example.shoppingcart.service.dto.ProductDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductResource {

    private final AdminProductService adminProductService;

    public AdminProductResource(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("")
    public ResponseEntity<List<AdminProductDTO>> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean active,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        Page<AdminProductDTO> page = adminProductService.findAll(search, active, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("")
    public ResponseEntity<AdminProductDTO> create(@Valid @RequestBody ProductDTO request) throws URISyntaxException {
        AdminProductDTO result = adminProductService.create(request);
        return ResponseEntity.created(new URI("/api/admin/products/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminProductDTO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO request) {
        return ResponseEntity.ok(adminProductService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<AdminProductDTO> setActive(@PathVariable Long id, @Valid @RequestBody ProductActiveRequest request) {
        return ResponseEntity.ok(adminProductService.setActive(id, request.active()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminProductService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
