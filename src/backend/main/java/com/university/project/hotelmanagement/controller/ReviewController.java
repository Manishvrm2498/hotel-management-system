package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.ReviewRequestDTO;
import com.university.project.hotelmanagement.dto.ReviewResponseDTO;
import com.university.project.hotelmanagement.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<ReviewResponseDTO> addReview(@RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(reviewService.postReview(request));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ReviewResponseDTO>> getHotelReviews(@PathVariable Long hotelId) {
        return ResponseEntity.ok(reviewService.getReviewsByHotel(hotelId));
    }
}