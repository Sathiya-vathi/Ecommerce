package com.website.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.website.Controller.ProductController;
import com.website.dto.ProductDTO;
import com.website.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest 
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean 
    private ProductService productService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddProduct() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));

        when(productService.addProduct(any(ProductDTO.class))).thenReturn(product);

        mockMvc.perform(post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateProduct() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setName("Updated Product");
        product.setPrice(BigDecimal.valueOf(150));

        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(product);

        mockMvc.perform(put("/api/products/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAddProductForbiddenForUser() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testUpdateProductForbiddenForUser() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setName("Updated Product");
        product.setPrice(BigDecimal.valueOf(150));

        mockMvc.perform(put("/api/products/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testDeleteProductForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/products/delete/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllProducts() throws Exception {
        List<ProductDTO> products = Arrays.asList(new ProductDTO(), new ProductDTO());
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetProductById() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("Sample Product");
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sample Product"));
    }
}
