package com.example.foodfactory.service;

import com.example.foodfactory.entity.*;
import com.example.foodfactory.exception.*;
import com.example.foodfactory.repository.*;
import com.example.foodfactory.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Address address = null;
        if (request.getStreet() != null || request.getPincode() != null) {
            address = Address.builder()
                    .street(request.getStreet())
                    .pincode(request.getPincode())
                    .build();
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(address)
                .type(request.getType())
                .build();

        userRepository.save(user);
        logger.info("User registered successfully: {}", request.getEmail());

        // Send welcome email asynchronously (failures are caught inside EmailService)
        emailService.sendRegistrationEmail(user.getEmail(), user.getName());

        return AuthResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .type(user.getType().name())
                .message("Registration successful")
                .build();
    }

    /**
     * Called after httpRequest.login() has already verified credentials and
     * stored the Authentication in the session. Just loads the user for the response.
     */
    public AuthResponse getAuthResponse(String email) {
        logger.info("Building auth response for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return AuthResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .type(user.getType().name())
                .message("Login successful")
                .build();
    }
}
