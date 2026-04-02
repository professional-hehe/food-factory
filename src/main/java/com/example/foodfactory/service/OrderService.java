package com.example.foodfactory.service;

import com.example.foodfactory.entity.*;
import com.example.foodfactory.enums.*;
import com.example.foodfactory.exception.*;
import com.example.foodfactory.repository.*;
import com.example.foodfactory.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemsRepository orderItemRepository;
    @Autowired private OrderHistoryRepository orderHistoryRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;

    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {
        logger.info("Placing order for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new BadRequestException("Cart is empty. Add items before placing an order."));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty. Add items before placing an order.");
        }

        Restaurant restaurant = cartItems.get(0).getMenuItem().getRestaurant();

        BigDecimal totalPrice = cartItems.stream()
                .map(ci -> ci.getMenuItem().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .status(OrderStatus.PLACED)
                .totalPrice(totalPrice)
                .paymentType(request.getPaymentType())
                .build();
        order = orderRepository.save(order);

        // Save order items with snapshot price
        final Order savedOrder = order;
        List<OrderItem> orderItems = cartItems.stream().map(ci -> OrderItem.builder()
                .order(savedOrder)
                .menuItem(ci.getMenuItem())
                .quantity(ci.getQuantity())
                .price(ci.getMenuItem().getPrice())
                .build()
        ).collect(Collectors.toList());
        orderItemRepository.saveAll(orderItems);

        // Save order history entry
        OrderHistory history = OrderHistory.builder()
                .order(savedOrder)
                .status(OrderStatus.PLACED)
                .build();
        orderHistoryRepository.save(history);

        // Clear cart after order
        cartItemRepository.deleteByCart(cart);
        logger.info("Order #{} placed successfully for user: {}", savedOrder.getOrderId(), email);

        // Send confirmation email asynchronously
        emailService.sendOrderConfirmationEmail(
                email, user.getName(), savedOrder.getOrderId(),
                restaurant.getName(), totalPrice.toString());

        return buildOrderResponse(savedOrder, orderItems);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        logger.info("Cancelling order #{} for user: {}", orderId, email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .build();
        orderHistoryRepository.save(history);

        logger.info("Order #{} cancelled", orderId);

        // Send cancellation email asynchronously
        emailService.sendOrderCancellationEmail(
                email, order.getUser().getName(), orderId, order.getRestaurant().getName());

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        return buildOrderResponse(order, orderItems);
    }

    public List<OrderResponse> getMyOrders(String email) {
        logger.debug("Fetching orders for user: {}", email);
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(order -> buildOrderResponse(order, orderItemRepository.findByOrder(order)))
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        return buildOrderResponse(order, orderItems);
    }

    // Admin: update order status
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status(newStatus)
                .build();
        orderHistoryRepository.save(history);

        logger.info("Order #{} status updated to {}", orderId, newStatus);
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        return buildOrderResponse(order, orderItems);
    }

    // Admin: get all orders for a restaurant
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(order -> buildOrderResponse(order, orderItemRepository.findByOrder(order)))
                .collect(Collectors.toList());
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItem> orderItems) {
        List<OrderResponse.OrderItemDTO> itemDTOs = orderItems.stream().map(oi -> {
            BigDecimal subtotal = oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
            return OrderResponse.OrderItemDTO.builder()
                    .menuItemId(oi.getMenuItem().getMenuItemId())
                    .itemName(oi.getMenuItem().getItem().getName())
                    .category(oi.getMenuItem().getItem().getCategory() != null
                            ? oi.getMenuItem().getItem().getCategory().name() : null)
                    .quantity(oi.getQuantity())
                    .price(oi.getPrice())
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userEmail(order.getUser().getEmail())
                .userName(order.getUser().getName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .paymentType(order.getPaymentType().name())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .build();
    }
}
