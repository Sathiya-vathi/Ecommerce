package com.website.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.website.Dao.CartRepository;
import com.website.Dao.CartItemRepository;
import com.website.Dao.ProductRepository;
import com.website.Dao.UserRepository;
import com.website.dto.CartDTO;
import com.website.dto.CartItemDTO;
import com.website.entities.Cart;
import com.website.entities.CartItem;
import com.website.entities.Product;
import com.website.entities.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("User authentication failed: No authenticated user");
            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();
        logger.debug("Authenticated user: {}", email);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.error("Authenticated user not found in DB: {}", email);
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public CartDTO addItemToCart(Long productId, int quantity) {
        logger.info("Adding item to cart: productId={}, quantity={}", productId, quantity);
        User user = getAuthenticatedUser();

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            logger.info("No cart found for user {}, creating new one", user.getEmail());
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setCartItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.error("Product not found: ID={}", productId);
                    return new RuntimeException("Product not found");
                });

        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem item = existingCartItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
            logger.debug("Updated quantity for existing cart item: {}", item.getId());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
            logger.debug("Added new cart item for product ID: {}", productId);
        }

        return mapToCartDTO(cart);
    }

    public CartDTO getCartByUser() {
        User user = getAuthenticatedUser();
        logger.info("Fetching cart for user: {}", user.getEmail());

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            logger.info("No cart found for user {}, creating new one", user.getEmail());
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        return mapToCartDTO(cart);
    }

    public CartDTO removeItemFromCart(Long productId) {
        User user = getAuthenticatedUser();
        logger.info("Removing product from cart: productId={}, user={}", productId, user.getEmail());

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> {
                    logger.error("Cart not found for user: {}", user.getEmail());
                    return new RuntimeException("Cart not found");
                });

        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (optionalCartItem.isEmpty()) {
            logger.warn("Cart item not found for product ID: {}", productId);
            throw new RuntimeException("Cart item not found for product ID: " + productId);
        }

        CartItem cartItemToRemove = optionalCartItem.get();
        cart.getCartItems().remove(cartItemToRemove);
        cartItemRepository.delete(cartItemToRemove);
        cartRepository.save(cart);
        logger.debug("Removed item from cart: {}", cartItemToRemove.getId());

        return mapToCartDTO(cart);
    }

    public void clearCart() {
        User user = getAuthenticatedUser();
        logger.info("Clearing cart for user: {}", user.getEmail());

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    logger.error("Cart not found for user: {}", user.getEmail());
                    return new RuntimeException("Cart not found");
                });

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
        logger.debug("Cart cleared for user: {}", user.getEmail());
    }

   
    private CartDTO mapToCartDTO(Cart cart) {
        List<CartItemDTO> cartItemsDTO = cart.getCartItems().stream()
            .map(item -> new CartItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getQuantity(),
                item.getProduct().getPrice(),
                item.getProduct().getDiscountType(),
                item.getProduct().getDiscountValue()
            ))
            .collect(Collectors.toList());

        return new CartDTO(cart.getId(), cart.getUser().getId(), cartItemsDTO);
    }
}


