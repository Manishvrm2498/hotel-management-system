package com.university.project.hotelmanagement.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({ "bookingId", "hotelName", "roomType", "userId", "firstName", "lastName", "email", "userEmail", "phoneNumber", "totalGuests", "checkIn", "checkOut", "totalPrice", "status" })
public class BookingResponseDTO {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice;
    private String status;
    private LocalDateTime bookedAt;

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Long userId;
    private String userEmail;
    private String phoneNumber;
    private int totalGuests;
    private String roomType;
    private String hotelName;

}
