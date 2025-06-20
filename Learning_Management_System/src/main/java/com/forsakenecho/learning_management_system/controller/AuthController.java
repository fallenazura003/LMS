package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.*;
import com.forsakenecho.learning_management_system.entity.User;
import com.forsakenecho.learning_management_system.enums.Status;
import com.forsakenecho.learning_management_system.jwt.JwtUtil;
import com.forsakenecho.learning_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .status(Status.valueOf("ACTIVE"))
                .build();

        userRepository.save(user);

        UserResponse userResponse = UserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .message("Tạo tài khoản thành công!")
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        return ResponseEntity.ok(auth.getPrincipal());
    }
}
