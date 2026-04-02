package com.example.foodfactory.controller;

import com.example.foodfactory.dto.ApiResponse;
import com.example.foodfactory.dto.OrderRequest;
import com.example.foodfactory.dto.OrderResponse;
import com.example.foodfactory.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        String email = userDetails.getUsername();
        logger.info("POST /api/orders/place - user: {}", email);
        OrderResponse response = orderService.placeOrder(email, request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        logger.info("GET /api/orders - user: {}", email);
        List<OrderResponse> orders = orderService.getMyOrders(email);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched", orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        String email = userDetails.getUsername();
        logger.info("GET /api/orders/{} - user: {}", orderId, email);
        OrderResponse response = orderService.getOrderById(email, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order fetched", response));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        String email = userDetails.getUsername();
        logger.info("PUT /api/orders/{}/cancel - user: {}", orderId, email);
        OrderResponse response = orderService.cancelOrder(email, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }
}
