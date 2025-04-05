package com.website.dto;

public class CartRequestDTO {
    private String email;
    private Long productId;
    private int quantity;
    private Long cartItemId; 

    //Constructors
    public CartRequestDTO() {}
    
    public CartRequestDTO(String email) { //Add this constructor
        this.email = email;
    }

    public CartRequestDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    //Getters & Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public Long getCartItemId() {  //Add getter
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {  //Add setter
        this.cartItemId = cartItemId;
    }
}
