
package com.credshield.credshield.service;

import com.credshield.credshield.dto.RegisterRequest;
import com.credshield.credshield.dto.LoginRequest;
import com.credshield.credshield.entity.User;
import com.credshield.credshield.entity.UserRole;
import com.credshield.credshield.repository.UserRepository;

import com.credshield.credshield.exception.DuplicateResourceException;
import com.credshield.credshield.exception.InvalidCredentialsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(RegisterRequest request) {

        logger.info("New user registration request received");

        if (userRepository.existsByEmail(request.getEmail())) {

            logger.warn("Registration failed: duplicate email");

            throw new DuplicateResourceException(
                    "Email is already registered");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        String encryptedPassword =
                passwordEncoder.encode(request.getPassword());

        user.setPassword(encryptedPassword);

        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        logger.info("User registration successful");

        return savedUser;
    }

    public User loginUser(LoginRequest request) {

        logger.info("User login attempt received");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed: user not found");

                    return new InvalidCredentialsException(
                            "Invalid email or password");
                });

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {

            logger.warn("Login failed: incorrect password");

            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        logger.info("User login successful");

        return user;
    }
}
