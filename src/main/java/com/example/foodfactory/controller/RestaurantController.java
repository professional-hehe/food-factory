package com.example.foodfactory.controller;

import com.example.foodfactory.dto.*;
import com.example.foodfactory.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getAllRestaurants(
            @RequestParam(required = false) String search) {
        logger.info("GET /api/restaurants - search: {}", search);
        List<RestaurantDTO> restaurants = (search != null && !search.isBlank())
                ? restaurantService.searchRestaurants(search)
                : restaurantService.getAllActiveRestaurants();
        return ResponseEntity.ok(ApiResponse.success("Restaurants fetched", restaurants));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurantById(@PathVariable Long id) {
        logger.info("GET /api/restaurants/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant fetched", restaurantService.getRestaurantById(id)));
    }

    @GetMapping("/food/search")
    public ResponseEntity<ApiResponse<List<FoodSearchResultDTO>>> searchFoodItems(
            @RequestParam String query) {
        logger.info("GET /api/restaurants/food/search - query: {}", query);
        return ResponseEntity.ok(ApiResponse.success("Food items fetched", menuService.searchFoodItems(query)));
    }
}
