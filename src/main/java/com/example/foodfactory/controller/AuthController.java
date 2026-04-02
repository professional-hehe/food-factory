package com.example.foodfactory.controller;

import com.example.foodfactory.dto.*;
import com.example.foodfactory.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        logger.info("POST /api/auth/register - email: {}", request.getEmail());
        AuthResponse response = authService.register(request);

        // Auto-login and save session after registration
        saveAuthToSession(request.getEmail(), request.getPassword(), httpRequest, httpResponse);

        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        logger.info("POST /api/auth/login - email: {}", request.getEmail());

        // Authenticate and explicitly save to session
        saveAuthToSession(request.getEmail(), request.getPassword(), httpRequest, httpResponse);

        AuthResponse response = authService.getAuthResponse(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

@GetMapping("/me")
public ResponseEntity<ApiResponse<AuthResponse>> me(
        @org.springframework.security.core.annotation.AuthenticationPrincipal
        org.springframework.security.core.userdetails.UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated"));
        }
        logger.info("GET /api/auth/me - email: {}", principal.getUsername());
        AuthResponse response = authService.getAuthResponse(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Session active", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        logger.info("POST /api/auth/logout");
        SecurityContextHolder.clearContext();
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }

    /**
     * Authenticates the user, stores the Authentication in the SecurityContext,
     * and persists it in the HttpSession so subsequent requests are recognized.
     */
    private void saveAuthToSession(String email, String password,
                                   HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // This is the critical step — saves context into HttpSession
        securityContextRepository.saveContext(context, request, response);

        logger.info("Authentication saved to session for: {} with roles: {}", email, auth.getAuthorities());
    }
}
