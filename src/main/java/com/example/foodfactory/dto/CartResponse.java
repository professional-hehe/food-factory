package com.example.foodfactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {
    private Long cartId;
    private String userEmail;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CartItemDTO {
        private Long cartItemId;
        private Long menuItemId;
        private String itemName;
        private String category;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
