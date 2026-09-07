package com.ordering.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String phoneNumber,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            String address,
            LocalDate birthday
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record UpdateRoleRequest(
            @NotBlank String role // e.g. "ROLE_ADMIN" or "ROLE_CUSTOMER"
    ) {}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UserResponse user
    ) {}

    public record UserResponse(
            UUID uuid,
            String firstName,
            String lastName,
            String email,
            String role
    ) {}
}
