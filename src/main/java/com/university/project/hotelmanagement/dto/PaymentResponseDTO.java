package com.university.project.hotelmanagement.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({"id", "transactionId", "district", "hotelName", "amount", "paymentStatus", "paymentMethod", "paymentTime", "checkInDate", "checkOutDate"})
public class PaymentResponseDTO {
    private Long id;
    private String transactionId;
    private Double amount;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String hotelName;
    private String district;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}