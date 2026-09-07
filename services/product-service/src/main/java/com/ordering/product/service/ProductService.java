package com.ordering.product.service;

import com.ordering.common.events.OrderCreatedEvent;
import com.ordering.common.events.StockReservationResultEvent;
import com.ordering.product.dto.ProductDtos.CreateRequest;
import com.ordering.product.dto.ProductDtos.Response;
import com.ordering.product.dto.ProductDtos.UpdateRequest;
import com.ordering.product.event.StockReservationPublisher;
import com.ordering.product.exception.ProductNotFoundException;
import com.ordering.product.model.Product;
import com.ordering.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StockReservationPublisher publisher;

    // ---- Plain CRUD, same shape as the monolith's ProductService ----

    public Response create(CreateRequest req) {
        Product p = new Product(req.name(), req.description(), req.price(), req.stock());
        return toResponse(productRepository.save(p));
    }

    public Response get(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<Response> list() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Response update(Long id, UpdateRequest req) {
        Product p = findOrThrow(id);
        if (req.name() != null) p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        if (req.price() != null) p.setPrice(req.price());
        if (req.stock() != null) p.setStock(req.stock());
        return toResponse(productRepository.save(p));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) throw new ProductNotFoundException(id);
        productRepository.deleteById(id);
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Response toResponse(Product p) {
        return new Response(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock());
    }

    // ---- Stock reservation, triggered by OrderCreatedEvent ----

    /**
     * All-or-nothing reservation for one order: locks every product row involved,
     * checks every line has enough stock, and only then decrements. If any line
     * is short, nothing is decremented and we reply "rejected" so order-service
     * can cancel the order instead of leaving it partially fulfilled.
     */
    @Transactional
    public void reserveStockForOrder(OrderCreatedEvent event) {
        List<Long> productIds = event.getItems().stream()
                .map(OrderCreatedEvent.Item::getProductId)
                .distinct()
                .toList();

        List<Product> locked = productRepository.findAllForUpdate(productIds);
        Map<Long, Product> byId = locked.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderCreatedEvent.Item item : event.getItems()) {
            Product product = byId.get(item.getProductId());
            if (product == null) {
                reject(event.getOrderId(), "Product not found: " + item.getProductId());
                return;
            }
            if (product.getStock() < item.getQuantity()) {
                reject(event.getOrderId(), "Insufficient stock for productId=" + product.getId()
                        + " (requested=" + item.getQuantity() + ", available=" + product.getStock() + ")");
                return;
            }
        }

        // Every line checked out — now actually decrement.
        for (OrderCreatedEvent.Item item : event.getItems()) {
            Product product = byId.get(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        log.info("Stock reserved for orderId={}", event.getOrderId());
        publisher.publish(new StockReservationResultEvent(event.getOrderId(), true, null));
    }

    private void reject(Long orderId, String reason) {
        log.warn("Stock reservation rejected for orderId={}: {}", orderId, reason);
        publisher.publish(new StockReservationResultEvent(orderId, false, reason));
    }
}
