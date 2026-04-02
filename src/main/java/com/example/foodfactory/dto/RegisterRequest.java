package com.example.foodfactory.dto;

import com.example.foodfactory.enums.UserType;

import lombok.Data;

@Data
public class RegisterRequest {

    private String name;

    private String email;
    private String password;

    private String phone;
    private String street;
    private String pincode;

    private UserType type = UserType.CUSTOMER;
}
