package com.website.dto;
import java.math.BigDecimal;
import java.util.List;


public class CartDTO {
    private Long cartId;
    private int userId;
    private List<CartItemDTO> cartItems;
    private BigDecimal totalCartAmount;

    public CartDTO(Long cartId, int userId, List<CartItemDTO> cartItems) {
        this.cartId = cartId;
        this.userId = userId;
        this.cartItems = cartItems;

        // Compute totalCartAmount using BigDecimal
        this.totalCartAmount = cartItems.stream()
            .map(CartItemDTO::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public CartDTO() {
    }

    // Add Getters & Setters
    public Long getCartId() { return cartId; }
    public int getUserId() { return userId; }
    public List<CartItemDTO> getCartItems() { return cartItems; }
    public BigDecimal getTotalCartAmount() { return totalCartAmount; }
	public void setCartId(Long cartId) {
		this.cartId = cartId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public void setCartItems(List<CartItemDTO> cartItems) {
		this.cartItems = cartItems;
	}
	public void setTotalCartAmount(BigDecimal totalCartAmount) {
		this.totalCartAmount = totalCartAmount;
	}
    
    
}
