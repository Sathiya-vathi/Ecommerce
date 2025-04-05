package com.website.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.website.Controller.OrderController;
import com.website.Dao.OrderRepository;
import com.website.dto.OrderDTO;
import com.website.enums.OrderStatus;
import com.website.service.InvoiceService;
import com.website.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private InvoiceService invoiceService;
    
    @MockBean
    private OrderRepository orderRepository;

    
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testPlaceOrderWithUserRole() throws Exception {
        OrderDTO mockOrder = new OrderDTO(1L, 101, OrderStatus.PENDING, new BigDecimal("500.00"));
        Mockito.when(orderService.placeOrder()).thenReturn(mockOrder);

        mockMvc.perform(post("/orders/place")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(1))
            .andExpect(jsonPath("$.totalAmount").value(500.00));
    }

    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateOrderStatusWithAdminRole() throws Exception {
        mockMvc.perform(put("/orders/update-status/1")
                .param("status", "SHIPPED")
                .with(csrf()))
            .andExpect(status().isOk());
    }


   
    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/orders/1"))
            .andExpect(status().isUnauthorized());
    }

 

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testVerifyPayment_Success() throws Exception {
        mockMvc.perform(get("/orders/verify-payment")
                .param("session_id", "validSession123"))
            .andExpect(status().isBadRequest()); 
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllOrdersWithAdminRole() throws Exception {
        List<OrderDTO> mockOrders = List.of(
            new OrderDTO(1L, 101, OrderStatus.PENDING, new BigDecimal("500.00")),
            new OrderDTO(2L, 102, OrderStatus.SHIPPED, new BigDecimal("1000.00"))
        );

        Mockito.when(orderService.getAllOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/orders/all"))
            .andExpect(status().isOk()) 
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].orderId").value(1))
            .andExpect(jsonPath("$[1].orderId").value(2));
    }


   
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testFilterOrdersByPriceWithAdminRole() throws Exception {
        List<OrderDTO> filteredOrders = List.of(
            new OrderDTO(1L, 101, OrderStatus.PENDING, new BigDecimal("700.00"))
        );

        Mockito.when(orderService.filterOrdersByPrice(any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(filteredOrders);

        mockMvc.perform(get("/orders/filter-by-price")
                .param("minPrice", "500")
                .param("maxPrice", "1000"))
            .andExpect(status().isOk()) 
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].totalAmount").value(700.00));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFilterOrdersByPriceWithUserRoleForbidden() throws Exception {

        Mockito.when(orderService.filterOrdersByPrice(any(BigDecimal.class), any(BigDecimal.class)))
               .thenReturn(List.of()); 

        mockMvc.perform(get("/orders/filter-by-price")
        	       .param("minPrice", "500")
        	       .param("maxPrice", "1000"))
        	       .andExpect(status().isOk())
        	       .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        	       .andExpect(jsonPath("$.length()").isNotEmpty());

    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testCancelOrder_Success() throws Exception {
        Long orderId = 1L;


        Map<String, Object> mockResponse = Map.of("status", "success", "message", "Order cancelled successfully");
        Mockito.when(orderService.cancelOrder(orderId)).thenReturn(mockResponse);

        mockMvc.perform(put("/orders/cancel/{orderId}", orderId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testCancelOrder_Failure() throws Exception {
        Long orderId = 99L;

        Mockito.when(orderService.cancelOrder(orderId))
            .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(put("/orders/cancel/{orderId}", orderId)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.message").value("Order not found"));
    }


}
