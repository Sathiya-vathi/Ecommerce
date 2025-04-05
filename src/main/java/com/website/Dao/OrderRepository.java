package com.website.Dao;

import com.website.entities.Order;
import com.website.entities.User;
import com.website.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByUser(User user);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByTotalAmountBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi WHERE o.user.id = :userId AND oi.product.id = :productId")
    boolean existsByUserIdAndProductId(@Param("userId") int userId, @Param("productId") Long productId);

}
