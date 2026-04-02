package com.example.foodfactory.controller;

import com.example.foodfactory.dto.ApiResponse;
import com.example.foodfactory.dto.MenuItemDTO;
import com.example.foodfactory.dto.OrderResponse;
import com.example.foodfactory.dto.RestaurantDTO;
import com.example.foodfactory.enums.OrderStatus;
import com.example.foodfactory.service.MenuService;
import com.example.foodfactory.service.OrderService;
import com.example.foodfactory.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private OrderService orderService;

    // ── Restaurant Management ──────────────────────────────────────

    @GetMapping("/restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getAllRestaurants() {
        logger.info("ADMIN GET /api/admin/restaurants");
        return ResponseEntity.ok(ApiResponse.success("Restaurants fetched", restaurantService.getAllRestaurants()));
    }

    @PutMapping("/restaurants/{id}/status")
    public ResponseEntity<ApiResponse<RestaurantDTO>> toggleRestaurantStatus(
            @PathVariable Long id, @RequestParam boolean active) {
        logger.info("ADMIN PUT /api/admin/restaurants/{}/status - active: {}", id, active);
        return ResponseEntity.ok(ApiResponse.success("Restaurant status updated", restaurantService.setRestaurantActive(id, active)));
    }

    @PostMapping("/restaurants")
    public ResponseEntity<ApiResponse<RestaurantDTO>> addRestaurant(@RequestBody RestaurantDTO dto) {
        logger.info("ADMIN POST /api/admin/restaurants - name: {}", dto.getName());
        return ResponseEntity.ok(ApiResponse.success("Restaurant added", restaurantService.addRestaurant(dto)));
    }

    @PutMapping("/restaurants/{id}")
    public ResponseEntity<ApiResponse<RestaurantDTO>> updateRestaurant(
            @PathVariable Long id, @RequestBody RestaurantDTO dto) {
        logger.info("ADMIN PUT /api/admin/restaurants/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated", restaurantService.updateRestaurant(id, dto)));
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable Long id) {
        logger.info("ADMIN DELETE /api/admin/restaurants/{}", id);
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant deactivated"));
    }

    // ── Menu Management ───────────────────────────────────────────

    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<ApiResponse<List<MenuItemDTO>>> getMenuByRestaurant(
            @PathVariable Long restaurantId) {
        logger.info("ADMIN GET /api/admin/restaurants/{}/menu", restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Menu fetched", menuService.getAllMenuByRestaurant(restaurantId)));
    }

    @PutMapping("/menu/repair")
    public ResponseEntity<ApiResponse<String>> repairMenuItems() {
        logger.info("ADMIN PUT /api/admin/menu/repair - fixing unavailable/broken items");
        menuService.repairAllMenuItems();
        return ResponseEntity.ok(ApiResponse.success("All menu items repaired (set to available)"));
    }

    @PutMapping("/menu/{menuItemId}/availability")
    public ResponseEntity<ApiResponse<MenuItemDTO>> toggleMenuItemAvailability(
            @PathVariable Long menuItemId, @RequestParam boolean available) {
        logger.info("ADMIN PUT /api/admin/menu/{}/availability - available: {}", menuItemId, available);
        return ResponseEntity.ok(ApiResponse.success("Availability updated", menuService.setMenuItemAvailable(menuItemId, available)));
    }

    @PostMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<ApiResponse<MenuItemDTO>> addMenuItem(
            @PathVariable Long restaurantId, @RequestBody MenuItemDTO dto) {
        logger.info("ADMIN POST /api/admin/restaurants/{}/menu - item: {}", restaurantId, dto.getItemName());
        return ResponseEntity.ok(ApiResponse.success("Menu item added", menuService.addMenuItem(restaurantId, dto)));
    }

    @PutMapping("/menu/{menuItemId}")
    public ResponseEntity<ApiResponse<MenuItemDTO>> updateMenuItem(
            @PathVariable Long menuItemId, @RequestBody MenuItemDTO dto) {
        logger.info("ADMIN PUT /api/admin/menu/{}", menuItemId);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated", menuService.updateMenuItem(menuItemId, dto)));
    }

    @DeleteMapping("/menu/{menuItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long menuItemId) {
        logger.info("ADMIN DELETE /api/admin/menu/{}", menuItemId);
        menuService.deleteMenuItem(menuItemId);
        return ResponseEntity.ok(ApiResponse.success("Menu item deactivated"));
    }

    // ── Order Management ──────────────────────────────────────────

    @GetMapping("/restaurants/{restaurantId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByRestaurant(
            @PathVariable Long restaurantId) {
        logger.info("ADMIN GET /api/admin/restaurants/{}/orders", restaurantId);
        List<OrderResponse> orders = orderService.getOrdersByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched", orders));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId, @RequestParam OrderStatus status) {
        logger.info("ADMIN PUT /api/admin/orders/{}/status - status: {}", orderId, status);
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated to " + status, response));
    }
}
