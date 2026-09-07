package com.ordering.auth.repository;

import com.ordering.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Page<User> findAllByOrderByEmailAsc(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
