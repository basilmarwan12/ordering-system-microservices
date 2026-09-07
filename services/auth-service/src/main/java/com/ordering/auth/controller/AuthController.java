package com.ordering.auth.controller;

import com.ordering.auth.dto.AuthDtos.AuthResponse;
import com.ordering.auth.dto.AuthDtos.LoginRequest;
import com.ordering.auth.dto.AuthDtos.RegisterRequest;
import com.ordering.auth.dto.AuthDtos.UpdateRoleRequest;
import com.ordering.auth.dto.AuthDtos.UserResponse;
import com.ordering.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse updateRole(@PathVariable("id") UUID userId, @Valid @RequestBody UpdateRoleRequest req) {
        return authService.updateRole(userId, req.role());
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return authService.listUsers(PageRequest.of(page, size));
    }
}
