package com.website.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.website.dto.CartDTO;
import com.website.dto.CartRequestDTO;
import com.website.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"}) // Simulates an authenticated user
    void testAddItemToCart() throws Exception {
        CartRequestDTO request = new CartRequestDTO(1L, 2);
        CartDTO cartDTO = new CartDTO();

        when(cartService.addItemToCart(any(Long.class), any(Integer.class))).thenReturn(cartDTO);

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetCartItems() throws Exception {
        CartDTO cartDTO = new CartDTO();
        when(cartService.getCartByUser()).thenReturn(cartDTO);

        mockMvc.perform(get("/api/cart/items"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testRemoveItemFromCart() throws Exception {
        CartRequestDTO request = new CartRequestDTO(1L,0); // Only productId needed
        CartDTO cartDTO = new CartDTO();

        when(cartService.removeItemFromCart(any(Long.class))).thenReturn(cartDTO);

        mockMvc.perform(delete("/api/cart/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testClearCart() throws Exception {
        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart cleared successfully"));
    }
}
