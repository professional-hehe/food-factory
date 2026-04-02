package com.example.foodfactory.controller;

import com.example.foodfactory.dto.*;
import com.example.foodfactory.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        logger.info("GET /api/cart - user: {}", email);
        return ResponseEntity.ok(ApiResponse.success("Cart fetched", cartService.getCart(email)));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartRequest request) {
        String email = userDetails.getUsername();
        logger.info("POST /api/cart/add - user: {}, menuItemId: {}", email, request.getMenuItemId());
        CartResponse response = cartService.addToCart(email, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", response));
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        String email = userDetails.getUsername();
        logger.info("PUT /api/cart/update/{} - user: {}, qty: {}", cartItemId, email, quantity);
        CartResponse response = cartService.updateCartItem(email, cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", response));
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        String email = userDetails.getUsername();
        logger.info("DELETE /api/cart/remove/{} - user: {}", cartItemId, email);
        CartResponse response = cartService.removeFromCart(email, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", response));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        logger.info("DELETE /api/cart/clear - user: {}", email);
        cartService.clearCart(email);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
