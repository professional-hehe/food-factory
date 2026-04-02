package com.example.foodfactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuItemDTO {
    private Long menuItemId;
    private Long itemId;
    private String itemName;
    private String description;
    private String category;
    private BigDecimal price;
    private Boolean available;
    private Long restaurantId;
    private String restaurantName;
}
