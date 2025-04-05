package com.website.Controller;


import com.website.dto.ReviewDTO;
import com.website.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    //Add a review
    @PostMapping("/add")
    public ResponseEntity<ReviewDTO> addReview(@RequestParam String email,
                                               @RequestParam Long productId,
                                               @RequestParam int rating,
                                               @RequestParam String comment) {
        return ResponseEntity.ok(reviewService.addReview(email, productId, rating, comment));
    }

    //Get all reviews for a product
    @GetMapping("/{productId}")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }
}
