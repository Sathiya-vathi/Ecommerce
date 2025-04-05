package com.website.service;

import com.website.Dao.*;
import com.website.dto.OrderDTO;
import com.website.dto.CartDTO;
import com.website.entities.*;
import com.website.enums.OrderStatus;
import com.website.service.OrderService;
import com.website.service.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;
    
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);


        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");


        testProduct = new Product();
        testProduct.setId(100L);
        testProduct.setPrice(BigDecimal.valueOf(50));


        testCartItem = new CartItem();
        testCartItem.setId(10L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);


        testCart = new Cart();
        testCart.setId(200L);
        testCart.setUser(testUser);
        testCart.setCartItems(Arrays.asList(testCartItem));

        
        testOrder = new Order();
        testOrder.setId(300L);
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(BigDecimal.valueOf(100));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setOrderDate(LocalDateTime.now());


        CartDTO mockCartDTO = mock(CartDTO.class);
        lenient().when(mockCartDTO.getTotalCartAmount()).thenReturn(BigDecimal.valueOf(100));


        lenient().when(cartService.getAuthenticatedUser()).thenReturn(testUser);

        lenient().when(cartService.getCartByUser()).thenReturn(mockCartDTO);


        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);
        lenient().when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));


        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(300L);
            return savedOrder;
        });
    }



    @Test
    void testGetCartTotal() {
        CartDTO mockCartDTO = mock(CartDTO.class);
        when(mockCartDTO.getTotalCartAmount()).thenReturn(BigDecimal.valueOf(100));
        when(cartService.getCartByUser()).thenReturn(mockCartDTO);

        BigDecimal total = orderService.getCartTotal();

        assertEquals(BigDecimal.valueOf(100), total);
    }

    @Test
    void testPlaceOrder() {
        when(cartService.getAuthenticatedUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        
        CartDTO mockCartDTO = mock(CartDTO.class);
        when(mockCartDTO.getTotalCartAmount()).thenReturn(BigDecimal.valueOf(100));
        when(cartService.getCartByUser()).thenReturn(mockCartDTO);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(300L);
            return savedOrder;
        });

        OrderDTO orderDTO = orderService.placeOrder();

        assertNotNull(orderDTO);
        assertEquals(300L, orderDTO.getOrderId());
        verify(cartItemRepository, times(1)).deleteAll(testCart.getCartItems());
    }

    @Test
    void testPlaceOrderWithEmptyCart() {
        testCart.setCartItems(Collections.emptyList());

        when(cartService.getAuthenticatedUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        Exception exception = assertThrows(RuntimeException.class, orderService::placeOrder);
        assertEquals("❌ Cart is empty, cannot place order", exception.getMessage());
    }

    @Test
    void testGetOrderById() {
        when(orderRepository.findById(300L)).thenReturn(Optional.of(testOrder));
        OrderDTO orderDTO = orderService.getOrderById(300L);
        assertNotNull(orderDTO);
        assertEquals(300L, orderDTO.getOrderId());
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById(300L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDTO updatedOrder = orderService.updateOrderStatus(300L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, updatedOrder.getStatus());
    }

    @Test
    void testGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        List<OrderDTO> orders = orderService.getAllOrders();
        assertFalse(orders.isEmpty());
        assertEquals(1, orders.size());
    }
    
    @Test
    void testCancelOrderSuccess() {
        testOrder.setStatus(OrderStatus.PLACED);
        when(cartService.getAuthenticatedUser()).thenReturn(testUser);
        when(orderRepository.findById(300L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Map<String, Object> response = orderService.cancelOrder(300L);

        assertEquals("success", response.get("status"));
        assertEquals(300L, response.get("orderId"));
        assertEquals("✅ Order cancelled. Amount will be refunded shortly.", response.get("message"));
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
    }

    @Test
    void testCancelOrderUnauthorizedUser() {
        User otherUser = new User();
        otherUser.setId(999);
        when(cartService.getAuthenticatedUser()).thenReturn(otherUser);

        testOrder.setUser(testUser); 
        when(orderRepository.findById(300L)).thenReturn(Optional.of(testOrder));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(300L));
        assertEquals("❌ You are not authorized to cancel this order", exception.getMessage());
    }

    @Test
    void testCancelOrderAlreadyShipped() {
        when(cartService.getAuthenticatedUser()).thenReturn(testUser);
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(300L)).thenReturn(Optional.of(testOrder));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(300L));
        assertEquals("❌ Cannot cancel an order that has already been shipped or delivered", exception.getMessage());
    }

    @Test
    void testCancelOrderAlreadyDelivered() {
        when(cartService.getAuthenticatedUser()).thenReturn(testUser);
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(300L)).thenReturn(Optional.of(testOrder));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(300L));
        assertEquals("❌ Cannot cancel an order that has already been shipped or delivered", exception.getMessage());
    }

    @Test
    void testCancelOrderNotFound() {
        when(cartService.getAuthenticatedUser()).thenReturn(testUser);
        when(orderRepository.findById(300L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(300L));
        assertEquals("Order not found", exception.getMessage());
    }

}
