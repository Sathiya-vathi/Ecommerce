package com.website.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.website.dto.CartDTO;
import com.website.dto.CartRequestDTO;
import com.website.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    //Add item to cart using DTO
    @PostMapping("/add")
    public ResponseEntity<CartDTO> addItemToCart(@RequestBody CartRequestDTO cartRequest) {
        return ResponseEntity.ok(cartService.addItemToCart(
            cartRequest.getProductId(),
            cartRequest.getQuantity()
        ));
    }

    //Get cart items using authentication instead of passing email manually
    @GetMapping("/items")
    public ResponseEntity<CartDTO> getCartItems() {
        return ResponseEntity.ok(cartService.getCartByUser());
    }

    @DeleteMapping("/remove")
    public ResponseEntity<CartDTO> removeItemFromCart(@RequestBody CartRequestDTO request) {
        if (request.getProductId() == null) {
            throw new RuntimeException("Product ID is required for removal");
        }
        return ResponseEntity.ok(cartService.removeItemFromCart(request.getProductId()));
    }




    //Clear cart using authentication instead of passing email
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok("Cart cleared successfully");
    }
}
