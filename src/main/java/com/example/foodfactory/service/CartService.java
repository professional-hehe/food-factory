package com.example.foodfactory.service;

import com.example.foodfactory.dto.CartRequest;
import com.example.foodfactory.dto.CartResponse;
import com.example.foodfactory.entity.*;
import com.example.foodfactory.exception.BadRequestException;
import com.example.foodfactory.exception.ResourceNotFoundException;
import com.example.foodfactory.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private UserRepository userRepository;

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return cartRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    Cart cart = Cart.builder().user(user).build();
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartResponse addToCart(String email, CartRequest request) {
        Cart cart = getOrCreateCart(email);

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()));

        if (!menuItem.getIsAvailable()) {
            throw new BadRequestException("Menu item is currently unavailable: " + menuItem.getItem().getName());
        }

        // Enforce single restaurant per cart
        List<CartItem> existingItems = cartItemRepository.findByCart(cart);
        if (!existingItems.isEmpty()) {
            Long existingRestaurantId = existingItems.get(0).getMenuItem().getRestaurant().getId();
            Long newRestaurantId = menuItem.getRestaurant().getId();
            if (!existingRestaurantId.equals(newRestaurantId)) {
                throw new BadRequestException(
                    "Cannot add items from different restaurants. Clear your cart first.");
            }
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndMenuItem(cart, menuItem);
        if (existingCartItem.isPresent()) {
            CartItem ci = existingCartItem.get();
            ci.setQuantity(ci.getQuantity() + request.getQuantity());
            cartItemRepository.save(ci);
            logger.info("Updated quantity for menuItem {} in cart for user {}", request.getMenuItemId(), email);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(cartItem);
            logger.info("Added menuItem {} to cart for user {}", request.getMenuItemId(), email);
        }

        return buildCartResponse(cart, email);
    }

    @Transactional
    public CartResponse updateCartItem(String email, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            logger.info("Removed cartItem {} from cart for user {}", cartItemId, email);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            logger.info("Updated cartItem {} quantity to {} for user {}", cartItemId, quantity, email);
        }

        return buildCartResponse(cart, email);
    }

    @Transactional
    public CartResponse removeFromCart(String email, Long cartItemId) {
        Cart cart = getOrCreateCart(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cartItemRepository.delete(cartItem);
        logger.info("Removed cartItem {} for user {}", cartItemId, email);
        return buildCartResponse(cart, email);
    }

    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return buildCartResponse(cart, email);
    }

    @Transactional
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteByCart(cart);
        logger.info("Cart cleared for user {}", email);
    }

    private CartResponse buildCartResponse(Cart cart, String email) {
        List<CartItem> items = cartItemRepository.findByCart(cart);

        List<CartResponse.CartItemDTO> itemDTOs = items.stream().map(ci -> {
            BigDecimal subtotal = ci.getMenuItem().getPrice()
                    .multiply(BigDecimal.valueOf(ci.getQuantity()));
            return CartResponse.CartItemDTO.builder()
                    .cartItemId(ci.getId())
                    .menuItemId(ci.getMenuItem().getMenuItemId())
                    .itemName(ci.getMenuItem().getItem().getName())
                    .category(ci.getMenuItem().getItem().getCategory() != null
                            ? ci.getMenuItem().getItem().getCategory().name() : null)
                    .price(ci.getMenuItem().getPrice())
                    .quantity(ci.getQuantity())
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal total = itemDTOs.stream()
                .map(CartResponse.CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userEmail(email)
                .items(itemDTOs)
                .totalAmount(total)
                .build();
    }
}
