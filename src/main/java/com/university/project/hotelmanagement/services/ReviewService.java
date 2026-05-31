package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.ReviewRequestDTO;
import com.university.project.hotelmanagement.dto.ReviewResponseDTO;
import com.university.project.hotelmanagement.entity.Booking;
import com.university.project.hotelmanagement.entity.Review;
import com.university.project.hotelmanagement.enums.BookingStatus;
import com.university.project.hotelmanagement.exception.BadRequestException;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.repository.BookingRepository;
import com.university.project.hotelmanagement.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewResponseDTO postReview(ReviewRequestDTO request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();


        if (!booking.getUser().getEmail().equals(currentUser)) {
            throw new BadRequestException("You can only review your own bookings!");
        }

        if (!BookingStatus.CONFIRMED.equals(booking.getStatus())
                && !BookingStatus.COMPLETED.equals(booking.getStatus())) {
            throw new BadRequestException("You can only review after a successful booking!");
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("You have already reviewed this stay.");
        }

        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setHotel(booking.getHotel());
        review.setUsers(booking.getUser());
        review.setBooking(booking);

        return mapToResponse(reviewRepository.save(review));
    }

    private ReviewResponseDTO mapToResponse(Review savedReview) {
        String firstName = savedReview.getUsers().getFirstName() == null ? "" : savedReview.getUsers().getFirstName();
        String lastName = savedReview.getUsers().getLastName() == null ? "" : savedReview.getUsers().getLastName();
        String username = (firstName + " " + lastName).trim();
        return ReviewResponseDTO.builder()
                .id(savedReview.getId())
                .rating(savedReview.getRating())
                .comment(savedReview.getComment())
                .username(username.isBlank() ? savedReview.getUsers().getEmail() : username)
                .userImageUrl("/api/users/" + savedReview.getUsers().getId() + "/picture")
                .hotelName(savedReview.getHotel().getName())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }


    public List<ReviewResponseDTO> getReviewsByHotel(Long hotelId) {
        return reviewRepository.findByHotelId(hotelId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
