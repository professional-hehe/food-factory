package com.example.foodfactory.service;

import com.example.foodfactory.entity.*;
import com.example.foodfactory.enums.*;
import com.example.foodfactory.exception.*;
import com.example.foodfactory.repository.*;
import com.example.foodfactory.dto.*;
import com.example.foodfactory.dto.FoodSearchResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<MenuItemDTO> getAllMenuByRestaurant(Long restaurantId) {
        logger.debug("Fetching all menu items (incl. unavailable) for restaurant: {}", restaurantId);
        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void repairAllMenuItems() {
        List<MenuItem> all = menuItemRepository.findAll();
        java.math.BigDecimal zero = java.math.BigDecimal.ZERO;
        for (MenuItem m : all) {
            boolean changed = false;
            if (Boolean.FALSE.equals(m.getIsAvailable())) {
                m.setIsAvailable(true);
                changed = true;
            }
            if (m.getPrice() == null || m.getPrice().compareTo(zero) <= 0) {
                m.setPrice(new java.math.BigDecimal("1.00"));
                changed = true;
            }
            if (changed) menuItemRepository.save(m);
        }
        logger.info("repairAllMenuItems: fixed {} items", all.size());
    }

    public MenuItemDTO setMenuItemAvailable(Long menuItemId, boolean available) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", menuItemId));
        menuItem.setIsAvailable(available);
        menuItemRepository.save(menuItem);
        logger.info("MenuItem {} availability set to {}", menuItemId, available);
        return toDTO(menuItem);
    }

    public List<FoodSearchResultDTO> searchFoodItems(String name) {
        logger.debug("Searching food items with name: {}", name);
        return menuItemRepository.searchByItemNameAvailable(name)
                .stream()
                .map(m -> FoodSearchResultDTO.builder()
                        .menuItemId(m.getMenuItemId())
                        .itemName(m.getItem().getName())
                        .description(m.getItem().getDescription())
                        .category(m.getItem().getCategory() != null ? m.getItem().getCategory().name() : null)
                        .price(m.getPrice())
                        .restaurantId(m.getRestaurant().getId())
                        .restaurantName(m.getRestaurant().getName())
                        .restaurantPhone(m.getRestaurant().getPhone())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getMenuByRestaurant(Long restaurantId) {
        logger.debug("Fetching menu for restaurant: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MenuItemDTO addMenuItem(Long restaurantId, MenuItemDTO dto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        Item item = Item.builder()
                .name(dto.getItemName())
                .description(dto.getDescription())
                .category(FoodCategory.valueOf(dto.getCategory()))
                .build();
        item = itemRepository.save(item);

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .item(item)
                .price(dto.getPrice())
                .isAvailable(true)
                .build();

        MenuItem saved = menuItemRepository.save(menuItem);
        logger.info("Menu item added: {} for restaurant: {}", dto.getItemName(), restaurantId);
        return toDTO(saved);
    }

    public MenuItemDTO updateMenuItem(Long menuItemId, MenuItemDTO dto) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", menuItemId));

        // Update MenuItem fields
        if (dto.getPrice() != null) menuItem.setPrice(dto.getPrice());
        if (dto.getAvailable() != null) menuItem.setIsAvailable(dto.getAvailable());

        // Update the linked Item entity and explicitly save it
        Item item = menuItem.getItem();
        if (dto.getItemName() != null && !dto.getItemName().isBlank()) {
            item.setName(dto.getItemName());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            item.setCategory(FoodCategory.valueOf(dto.getCategory()));
        }
        itemRepository.save(item); // ← explicitly persist Item changes

        return toDTO(menuItemRepository.save(menuItem));
    }

    public void deleteMenuItem(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", menuItemId));
        menuItem.setIsAvailable(false);
        menuItemRepository.save(menuItem);
        logger.info("MenuItem deactivated: {}", menuItemId);
    }

    public MenuItemDTO toDTO(MenuItem m) {
        return MenuItemDTO.builder()
                .menuItemId(m.getMenuItemId())
                .itemId(m.getItem().getItemId())
                .itemName(m.getItem().getName())
                .description(m.getItem().getDescription())
                .category(m.getItem().getCategory() != null ? m.getItem().getCategory().name() : null)
                .price(m.getPrice())
                .available(m.getIsAvailable())
                .restaurantId(m.getRestaurant().getId())
                .restaurantName(m.getRestaurant().getName())
                .build();
    }
}
