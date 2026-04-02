package com.example.foodfactory.dto;

import com.example.foodfactory.enums.PaymentType;

import lombok.Data;

@Data
public class OrderRequest {

    private PaymentType paymentType;
}
