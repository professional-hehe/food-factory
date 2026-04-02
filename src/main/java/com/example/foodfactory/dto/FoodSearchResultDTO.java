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
public class FoodSearchResultDTO {
    private Long menuItemId;
    private String itemName;
    private String description;
    private String category;
    private BigDecimal price;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantPhone;
}
