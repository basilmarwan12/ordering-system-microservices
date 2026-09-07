package com.ordering.auth.service;

import com.ordering.auth.dto.AuthDtos.AuthResponse;
import com.ordering.auth.dto.AuthDtos.LoginRequest;
import com.ordering.auth.dto.AuthDtos.RegisterRequest;
import com.ordering.auth.dto.AuthDtos.UserResponse;
import com.ordering.auth.exception.EmailAlreadyExistsException;
import com.ordering.auth.exception.InvalidCredentialsException;
import com.ordering.auth.exception.PhoneNumberAlreadyExistsException;
import com.ordering.auth.model.User;
import com.ordering.auth.repository.UserRepository;
import com.ordering.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyExistsException(req.email());
        }
        if (req.phoneNumber() != null && userRepository.existsByPhoneNumber(req.phoneNumber())) {
            throw new PhoneNumberAlreadyExistsException(req.phoneNumber());
        }

        User user = new User();
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPhoneNumber(req.phoneNumber());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setAddress(req.address());
        user.setBirthday(req.birthday());
        user.setRole("ROLE_CUSTOMER");

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return buildAuthResponse(user);
    }


    /**
     * Admin-only role change (see AuthController -- this endpoint requires an
     * existing ROLE_ADMIN token). There is no way to self-register as admin;
     * the first admin account has to be promoted directly in the database, or
     * via this endpoint using an admin token created that way. Document your
     * chosen bootstrap approach for real deployments -- this is deliberately
     * left manual rather than shipping a hidden default admin account.
     */
    @Transactional
    public UserResponse updateRole(java.util.UUID userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);
        return new UserResponse(saved.getUuid(), saved.getFirstName(), saved.getLastName(), saved.getEmail(), saved.getRole());
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.issueToken(user);
        UserResponse userResponse = new UserResponse(
                user.getUuid(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
        return new AuthResponse(token, "Bearer", jwtService.expirationSeconds(), userResponse);
    }

    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAllByOrderByEmailAsc(pageable)
                .map(u -> new UserResponse(u.getUuid(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getRole()));
    }
}
