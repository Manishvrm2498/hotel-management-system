package com.university.project.hotelmanagement.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonPropertyOrder({ "bookingId", "userName", "hotelName", "state", "district", "roomType", "checkIn", "checkOut", "amount" })
public class UserBookingDetailsDTO {
    private Long bookingId;
    private String userName;
    private String hotelName;
    private String state;
    private String district;
    private String roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double amount;
    private String status;
}