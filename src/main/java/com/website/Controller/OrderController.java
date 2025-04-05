package com.website.Controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.website.Dao.OrderRepository;
import com.website.dto.OrderDTO;
import com.website.entities.Order;
import com.website.enums.OrderStatus;
import com.website.service.InvoiceService;
import com.website.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:3000"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private OrderRepository orderRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private static final String STRIPE_SECRET_KEY = "sk_test_51R97XFC1QDzKXA5VHm5xUbMZwrVL4o5DNUNpypIZFHyBHCf5e9zdUmIOoBHZb6pVCtuyMv5JO62lTME8Hg1WBdec00MzV5Rgcf";

    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestParam Long orderId) {
        Stripe.apiKey = STRIPE_SECRET_KEY;

        try {
            //Use the already created order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            BigDecimal amount = order.getTotalAmount();

            logger.info("Creating payment session for Order ID: {}, amount: ₹{}", orderId, amount);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", " Invalid amount"));
            }

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:3000/cancel")
                    .putMetadata("order_id", orderId.toString())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("INR")
                                    .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Order Payment")
                                            .build())
                                    .build())
                            .build())
                    .build();

            Session session = Session.create(params);

            return ResponseEntity.ok(Map.of("sessionUrl", session.getUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error"));
        }
    }



    @GetMapping("/verify-payment")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestParam String session_id) {
        Stripe.apiKey = STRIPE_SECRET_KEY;

        try {
            logger.info("Verifying payment for session: {}", session_id);
            Session session = Session.retrieve(session_id);
            String paymentStatus = session.getPaymentStatus();
            logger.info("Payment status for session {}: {}", session_id, paymentStatus);

            if ("paid".equals(paymentStatus)) {
                String orderIdStr = session.getMetadata().get("order_id");
                Long orderId = Long.parseLong(orderIdStr);

                // Update order status to PLACED
                orderService.updateOrderStatus(orderId, OrderStatus.PLACED);
                logger.info("Order ID {} marked as PLACED after successful payment.", orderId);

                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Payment successful and order placed",
                    "orderId", orderId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed",
                    "message", "Payment not completed"
                ));
            }
        } catch (StripeException e) {
            logger.error("StripeException during verification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during verify-payment: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Internal Server Error"));
        }
    }


    //Place an Order
    @PostMapping("/place")
    public ResponseEntity<OrderDTO> placeOrder() {
        try {
            OrderDTO orderDTO = orderService.placeOrder();
            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    //Update Order Status by Admin
    @PutMapping("/update-status/{orderId}")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    //Get All Orders (Admin Only)
    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/filter-by-price")
    public ResponseEntity<List<OrderDTO>> filterOrdersByPrice(@RequestParam BigDecimal minPrice, @RequestParam BigDecimal maxPrice) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<OrderDTO> orders = orderService.filterOrdersByPrice(minPrice, maxPrice);
        return ResponseEntity.ok(orders);
    }
    
    //Get Order History for Authenticated User
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser() {
        List<OrderDTO> orders = orderService.getOrdersByUser();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        ByteArrayInputStream invoiceStream = invoiceService.generateInvoice(order);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=invoice-order-" + orderId + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(invoiceStream.readAllBytes());
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId) {
        try {
            Map<String, Object> response = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

}
