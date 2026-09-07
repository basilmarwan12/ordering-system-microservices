package com.ordering.product.controller;

import com.ordering.product.dto.ProductDtos.CreateRequest;
import com.ordering.product.dto.ProductDtos.Response;
import com.ordering.product.dto.ProductDtos.UpdateRequest;
import com.ordering.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req));
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) {
        return productService.get(id);
    }

    @GetMapping
    public Page<Response> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.list(PageRequest.of(validPage(page), boundedSize(size)));
    }

    private int validPage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        return page;
    }

    private int boundedSize(int size) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return size;
    }

    @PatchMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody UpdateRequest req) {
        return productService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
