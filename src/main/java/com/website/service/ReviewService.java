package com.website.service;

import com.website.Dao.ReviewRepository;
import com.website.Dao.OrderRepository;
import com.website.Dao.ProductRepository;
import com.website.Dao.UserRepository;
import com.website.dto.ReviewDTO;
import com.website.entities.Review;
import com.website.entities.User;
import com.website.exception.ReviewNotAllowedException;
import com.website.entities.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    //Add a Review (Only if the user purchased the product)
    public ReviewDTO addReview(String email, Long productId, int rating, String comment) {
        logger.info("🔹 Adding review for product ID: {} by user: {}", productId, email);

        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                String msg = "❌ User not found with email: " + email;
                logger.error(msg);
                throw new RuntimeException(msg);
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        String msg = "❌ Product not found with ID: " + productId;
                        logger.error(msg);
                        return new RuntimeException(msg);
                    });

            boolean hasPurchased = orderRepository.existsByUserIdAndProductId(user.getId(), productId);
            if (!hasPurchased) {
                String msg = "❌ User has not purchased this product.";
                logger.warn(msg);
                throw new ReviewNotAllowedException(msg);
            }

            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);
            review = reviewRepository.save(review);

            logger.info("✅ Review added successfully with ID: {}", review.getId());

            return new ReviewDTO(review.getId(), user.getId(), productId, rating, comment, review.getCreatedAt());

        } catch (RuntimeException e) {
            logger.error("❌ Error adding review: {}", e.getMessage(), e);
            throw e;
        }
    }

    //Get all reviews for a product
    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        logger.info("🔍 Fetching reviews for product ID: {}", productId);
        try {
            return reviewRepository.findByProductId(productId).stream()
                    .map(review -> new ReviewDTO(
                            review.getId(), review.getUser().getId(), productId,
                            review.getRating(), review.getComment(), review.getCreatedAt()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("❌ Failed to fetch reviews for product ID {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch reviews");
        }
    }
}
