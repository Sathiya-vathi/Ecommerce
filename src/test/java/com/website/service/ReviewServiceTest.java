package com.website.service;

import com.website.Dao.ReviewRepository;
import com.website.Dao.OrderRepository;
import com.website.Dao.ProductRepository;
import com.website.Dao.UserRepository;
import com.website.dto.ReviewDTO;
import com.website.entities.Review;
import com.website.entities.User;
import com.website.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    private User user;
    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId((int) 1L);
        user.setEmail("test@example.com");

        product = new Product();
        product.setId(100L);

        review = new Review();
        review.setId(10L);
        review.setUser(user);
        review.setProduct(product);
        review.setRating(5);
        review.setComment("Great product!");
        review.setCreatedAt(LocalDateTime.now());
    }


    @Test
    void testAddReview_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(orderRepository.existsByUserIdAndProductId((int) 1L, 100L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDTO result = reviewService.addReview("test@example.com", 100L, 5, "Great product!");

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great product!", result.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }


    @Test
    void testAddReview_Fail_NotPurchased() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(orderRepository.existsByUserIdAndProductId((int) 1L, 100L)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () ->
                reviewService.addReview("test@example.com", 100L, 5, "Great product!"));

        assertEquals("You can only review products you purchased.", exception.getMessage());
        verify(reviewRepository, never()).save(any(Review.class));
    }


    @Test
    void testGetReviewsByProduct() {
        when(reviewRepository.findByProductId(100L)).thenReturn(Arrays.asList(review));

        List<ReviewDTO> reviews = reviewService.getReviewsByProduct(100L);

        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        assertEquals(5, reviews.get(0).getRating());
        assertEquals("Great product!", reviews.get(0).getComment());
    }
}
