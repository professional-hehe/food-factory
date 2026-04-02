package com.example.foodfactory.service;

import com.example.foodfactory.entity.*;
import com.example.foodfactory.exception.*;
import com.example.foodfactory.repository.*;
import com.example.foodfactory.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<RestaurantDTO> getAllRestaurants() {
        logger.debug("Fetching all restaurants (active + inactive)");
        return restaurantRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public RestaurantDTO setRestaurantActive(Long id, boolean active) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        restaurant.setIsActive(active);
        restaurantRepository.save(restaurant);
        logger.info("Restaurant {} status set to active={}", id, active);
        return toDTO(restaurant);
    }

    public List<RestaurantDTO> getAllActiveRestaurants() {
        logger.debug("Fetching all active restaurants");
        return restaurantRepository.findByIsActiveTrue()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<RestaurantDTO> searchRestaurants(String name) {
        logger.debug("Searching restaurants with name: {}", name);
        return restaurantRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public RestaurantDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return toDTO(restaurant);
    }

    public RestaurantDTO addRestaurant(RestaurantDTO dto) {
        logger.info("Adding new restaurant: {}", dto.getName());
        Address address = Address.builder()
                .street(dto.getStreet())
                .pincode(dto.getPincode())
                .build();

        Restaurant restaurant = Restaurant.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .owner(dto.getOwner())
                .isActive(true)
                .address(address)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        logger.info("Restaurant saved with id: {}", saved.getId());
        return toDTO(saved);
    }

    public RestaurantDTO updateRestaurant(Long id, RestaurantDTO dto) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));

        restaurant.setName(dto.getName());
        restaurant.setPhone(dto.getPhone());
        restaurant.setIsActive(dto.getActive());

        if (restaurant.getAddress() != null) {
            restaurant.getAddress().setStreet(dto.getStreet());
            restaurant.getAddress().setPincode(dto.getPincode());
        } else {
            restaurant.setAddress(Address.builder()
                    .street(dto.getStreet())
                    .pincode(dto.getPincode())
                    .build());
        }

        return toDTO(restaurantRepository.save(restaurant));
    }

    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
        logger.info("Restaurant deactivated: {}", id);
    }

    private RestaurantDTO toDTO(Restaurant r) {
        return RestaurantDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .active(r.getIsActive())
                .phone(r.getPhone())
                .owner(r.getOwner())
                .street(r.getAddress() != null ? r.getAddress().getStreet() : null)
                .pincode(r.getAddress() != null ? r.getAddress().getPincode() : null)
                .build();
    }
}
