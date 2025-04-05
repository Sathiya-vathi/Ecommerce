package com.website.service;

import com.website.Dao.OrderRepository;
import com.website.Dao.CartRepository;
import com.website.Dao.CartItemRepository;
import com.website.Dao.UserRepository;
import com.website.dto.CartDTO;
import com.website.dto.OrderDTO;
import com.website.entities.*;
import com.website.enums.OrderStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;
    
    @Autowired
    private EmailService emailService;

    //Fetch Cart Total
    public BigDecimal getCartTotal() {
        try {
            User user = cartService.getAuthenticatedUser();
            logger.info("Fetching cart total for user ID: {}", user.getId());

            CartDTO cartDTO = cartService.getCartByUser();
            logger.debug("Cart contains {} items. Total amount: {}", cartDTO.getCartItems().size(), cartDTO.getTotalCartAmount());

            return cartDTO.getTotalCartAmount();
        } catch (Exception e) {
            logger.error("Error fetching cart total: {}", e.getMessage());
            throw new RuntimeException("Error fetching cart total");
        }
    }

    public OrderDTO placeOrder() {
        try {
            User user = cartService.getAuthenticatedUser();
            logger.info("Placing order for user ID: {}", user.getId());

            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> {
                        logger.error("Cart not found for user ID: {}", user.getId());
                        return new RuntimeException("Cart not found for user");
                    });

            if (cart.getCartItems().isEmpty()) {
                logger.warn("User ID {} attempted to place an order with an empty cart", user.getId());
                throw new RuntimeException("Cart is empty, cannot place order"); 
            }

            BigDecimal totalAmount = getCartTotal();

            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);
            order.setTotalAmount(totalAmount);

            List<OrderItem> orderItems = cart.getCartItems().stream().map(item -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                return orderItem;
            }).collect(Collectors.toList());

            order.setOrderItems(orderItems);
            orderRepository.save(order);

            cartItemRepository.deleteAll(cart.getCartItems());

            logger.info("Order placed successfully. Order ID: {}", order.getId());
            return new OrderDTO(order.getId(), user.getId(), order.getStatus(), order.getTotalAmount());

        } catch (RuntimeException e) {
            logger.error("Error placing order: {}", e.getMessage());
            throw e; 
        } catch (Exception e) {
            logger.error("Unexpected error placing order: {}", e.getMessage());
            throw new RuntimeException("Error placing order");
        }
    }


    //Get Order by ID
    public OrderDTO getOrderById(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.warn("Order not found with ID: {}", orderId);
                        return new RuntimeException("Order not found");
                    });

            return new OrderDTO(order.getId(), order.getUser().getId(), order.getStatus(), order.getTotalAmount());
        } catch (Exception e) {
            logger.error("Error fetching order by ID {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Error fetching order");
        }
    }

    //Get All Orders (Admin)
    public List<OrderDTO> getAllOrders() {
        try {
            logger.info("Fetching all orders (admin access)");
            return orderRepository.findAll().stream()
                    .map(order -> new OrderDTO(order.getId(), order.getUser().getId(), order.getStatus(), order.getTotalAmount()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all orders: {}", e.getMessage());
            throw new RuntimeException("Error fetching all orders");
        }
    }

    //Update Order Status
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        try {
            logger.info("Updating order status for Order ID: {} to {}", orderId, status);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.warn("Order not found for update with ID: {}", orderId);
                        return new RuntimeException("Order not found");
                    });

            order.setStatus(status);
            orderRepository.save(order);

            //Send Email Notification
            String userEmail = order.getUser().getEmail(); // Assuming your User entity has email
            emailService.sendOrderStatusEmail(userEmail, order.getId(), status.name());

            logger.info("Order ID {} status updated to {}. Email sent to {}", orderId, status, userEmail);
            return new OrderDTO(order.getId(), order.getUser().getId(), order.getStatus(), order.getTotalAmount());
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage());
            throw new RuntimeException("Error updating order status");
        }
    }


    //Filter Orders by Price
    public List<OrderDTO> filterOrdersByPrice(BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            logger.info("Filtering orders between ₹{} and ₹{}", minPrice, maxPrice);
            return orderRepository.findAll().stream()
                    .filter(order -> order.getTotalAmount().compareTo(minPrice) >= 0 && order.getTotalAmount().compareTo(maxPrice) <= 0)
                    .map(order -> new OrderDTO(order.getId(), order.getUser().getId(), order.getStatus(), order.getTotalAmount()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error filtering orders by price: {}", e.getMessage());
            throw new RuntimeException("Error filtering orders by price");
        }
    }

    //Get Orders by Authenticated User
    public List<OrderDTO> getOrdersByUser() {
        try {
            User user = cartService.getAuthenticatedUser();
            logger.info("Fetching orders for user ID: {}", user.getId());

            return orderRepository.findByUser(user).stream()
                    .map(order -> new OrderDTO(order.getId(), user.getId(), order.getStatus(), order.getTotalAmount()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching user order history: {}", e.getMessage());
            throw new RuntimeException("Error fetching order history");
        }
    }
    
    public Map<String, Object> cancelOrder(Long orderId) {
        try {
            User user = cartService.getAuthenticatedUser();
            logger.info("User ID {} requested cancellation for order ID {}", user.getId(), orderId);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (order.getUser().getId() != user.getId()) {
                throw new RuntimeException("You are not authorized to cancel this order");
            }

            if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
                throw new RuntimeException("Cannot cancel an order that has already been shipped or delivered");
            }

            String message;

            if (order.getStatus() == OrderStatus.PLACED) {
                message = "Order cancelled. Amount will be refunded shortly.";
            } else {
                message = "Order cancelled successfully.";
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            logger.info("Order ID {} cancelled by user ID {}", orderId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("orderId", orderId);
            response.put("message", message);

            return response;

        } catch (RuntimeException e) {
            logger.error("Error cancelling order: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            throw new RuntimeException("Internal Server Error");
        }
    }


}
