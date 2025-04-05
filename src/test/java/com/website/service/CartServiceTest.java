package com.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("1000.00"));
        product.setDiscountType("PERCENTAGE");
        product.setDiscountValue(new BigDecimal("10"));

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
    }

    @Test
    void testAddItemToCart() {
        setAuthentication();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartDTO cartDTO = cartService.addItemToCart(product.getId(), 1);

        assertNotNull(cartDTO);
        assertEquals(1, cartDTO.getCartItems().size());

        clearAuthentication();
    }

    @Test
    void testGetCartByUser() {
        setAuthentication();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CartDTO cartDTO = cartService.getCartByUser();

        assertNotNull(cartDTO);
        assertEquals(user.getId(), cartDTO.getUserId());

        clearAuthentication();
    }

 

    @Test
    void testClearCart() {
        setAuthentication();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        doNothing().when(cartItemRepository).deleteAll(cart.getCartItems());

        assertDoesNotThrow(() -> cartService.clearCart());

        clearAuthentication();
    }

    private void setAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
