package com.example.foodfactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private String userEmail;
    private String userName;
    private Long restaurantId;
    private String restaurantName;
    private String status;
    private BigDecimal totalPrice;
    private String paymentType;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemDTO {
        private Long menuItemId;
        private String itemName;
        private String category;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}
