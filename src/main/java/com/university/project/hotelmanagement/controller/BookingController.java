package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.BookingRequestDTO;
import com.university.project.hotelmanagement.dto.BookingResponseDTO;
import com.university.project.hotelmanagement.dto.UserBookingDetailsDTO;
import com.university.project.hotelmanagement.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> bookRoom(@Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO response = bookingService.createBooking(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Booking initiated successfully! Please proceed to payment to confirm your stay.");
        body.put("bookingId", response.getId());
        body.put("data", response);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponseDTO>> getUserBookings() {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUser();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings-details")
    public ResponseEntity<List<UserBookingDetailsDTO>> getMyBookings() {
        List<UserBookingDetailsDTO> myBookings = bookingService.getUserFullDetails();
        return ResponseEntity.ok(myBookings);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelMyBooking(@PathVariable Long id) {
        String message = bookingService.cancelBooking(id);
        return ResponseEntity.ok(message);
    }
}