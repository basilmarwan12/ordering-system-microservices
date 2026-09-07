package com.ordering.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class ProductDtos {

    private ProductDtos() {}

    public record CreateRequest(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @NotNull @Min(0) Integer stock
    ) {}

    public record UpdateRequest(
            String name,
            String description,
            @DecimalMin("0.0") BigDecimal price,
            @Min(0) Integer stock
    ) {}

    public record Response(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock
    ) {}
}
