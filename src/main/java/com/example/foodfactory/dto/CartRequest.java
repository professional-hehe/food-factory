package com.example.foodfactory.dto;

import lombok.Data;

@Data
public class CartRequest {

    private Long menuItemId;

    private Integer quantity;
}
