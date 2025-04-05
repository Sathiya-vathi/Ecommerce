package com.website.Dao;


import org.springframework.data.jpa.repository.JpaRepository;
import com.website.entities.CartItem;
import com.website.entities.Cart;
import com.website.entities.Product;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
