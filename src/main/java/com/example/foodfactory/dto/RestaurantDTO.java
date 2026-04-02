package com.example.foodfactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantDTO {
    private Long id;
    private String name;
    private Boolean active;
    private String phone;
    private String street;
    private String pincode;
    private String owner;
}

