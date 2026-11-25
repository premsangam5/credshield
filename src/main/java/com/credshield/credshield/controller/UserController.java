
package com.credshield.credshield.controller;

import com.credshield.credshield.dto.RegisterRequest;
import com.credshield.credshield.dto.LoginRequest;
import com.credshield.credshield.dto.UserResponse;

import com.credshield.credshield.entity.User;

import com.credshield.credshield.service.UserService;
import com.credshield.credshield.service.JwtService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(
            UserService userService,
            JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
    }

    // Registration API
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        User savedUser = userService.registerUser(request);

        Map<String, Object> response = Map.of(
                "message", "User registered successfully",
                "user", new UserResponse(savedUser)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Login API with JWT
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.loginUser(request);

        String token = jwtService.generateToken(user.getEmail());

        Map<String, Object> response = Map.of(
                "message", "Login successful",
                "user", new UserResponse(user),
                "token", token
        );

        return ResponseEntity.ok(response);
    }
}
