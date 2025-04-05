package com.website.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.website.Controller.ReviewController;
import com.website.dto.ReviewDTO;
import com.website.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAddReviewWithAuthenticatedUser() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO(1L, 1, 101L, 5, "Great product!", LocalDateTime.now());

        when(reviewService.addReview("user@example.com", 101L, 5, "Great product!"))
                .thenReturn(reviewDTO);

        mockMvc.perform(post("/api/reviews/add")
                .param("email", "user@example.com")
                .param("productId", "101")
                .param("rating", "5")
                .param("comment", "Great product!")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))  
                .andExpect(jsonPath("$.productId").value(101))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great product!"));
    }

    @Test
    void testAddReviewWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/reviews/add")
                .param("email", "user@example.com")
                .param("productId", "101")
                .param("rating", "5")
                .param("comment", "Great product!")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})  
    void testGetProductReviews() throws Exception {
        ReviewDTO review1 = new ReviewDTO(1L, 123, 101L, 5, "Great product!", LocalDateTime.now());
        ReviewDTO review2 = new ReviewDTO(2L, 124, 101L, 4, "Good quality!", LocalDateTime.now());

        List<ReviewDTO> mockReviews = Arrays.asList(review1, review2);

        when(reviewService.getReviewsByProduct(101L)).thenReturn(mockReviews);

        mockMvc.perform(get("/api/reviews/101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(123)) 
                .andExpect(jsonPath("$[1].comment").value("Good quality!")); 
    }
}
