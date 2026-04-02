package com.example.foodfactory.controller;

import com.example.foodfactory.dto.*;
import com.example.foodfactory.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private MenuService menuService;

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuItemDTO>>> getMenu(@PathVariable Long restaurantId) {
        logger.info("GET /api/menu/restaurant/{}", restaurantId);
        List<MenuItemDTO> menu = menuService.getMenuByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Menu fetched", menu));
    }
}
