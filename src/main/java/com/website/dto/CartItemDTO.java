package com.website.dto;

import java.math.BigDecimal;

public class CartItemDTO {
    private Long cartItemId;
    private Long productId;
    private int quantity;
    private BigDecimal price;  // Updated to store final price after discount

    public CartItemDTO(Long cartItemId, Long productId, int quantity, BigDecimal originalPrice, String discountType, BigDecimal discountValue) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = calculateDiscountedPrice(originalPrice, discountType, discountValue);
    }

    // Method to apply the discount
    private BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, String discountType, BigDecimal discountValue) {
        if (discountType == null || discountValue == null) {
            return originalPrice; // No discount, return original price
        }
        switch (discountType) {
            case "PERCENTAGE":
                return originalPrice.subtract(originalPrice.multiply(discountValue).divide(BigDecimal.valueOf(100)));
            case "FIXED":
                return originalPrice.subtract(discountValue).max(BigDecimal.ZERO);
            default:
                return originalPrice; // If invalid discount type, return original price
        }
    }

    public BigDecimal getTotalAmount() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters
    public Long getCartItemId() { return cartItemId; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; } // Returns the final discounted price
}
