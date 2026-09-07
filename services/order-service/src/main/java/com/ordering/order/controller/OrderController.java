package com.ordering.order.controller;

import com.ordering.order.dto.OrderDtos.CreateRequest;
import com.ordering.order.dto.OrderDtos.Response;
import com.ordering.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<Response> list() {
        return orderService.list();
    }
}
