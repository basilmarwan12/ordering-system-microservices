package com.ordering.order.controller;

import com.ordering.order.dto.OrderDtos.CreateRequest;
import com.ordering.order.dto.OrderDtos.Response;
import com.ordering.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(req));
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @GetMapping
    public Page<Response> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.list(PageRequest.of(validPage(page), boundedSize(size)));
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
}
