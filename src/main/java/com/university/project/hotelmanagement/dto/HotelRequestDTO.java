package com.university.project.hotelmanagement.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class HotelRequestDTO {

    @NotBlank(message = "Hotel name is required")
    @Size(max = 100, message = "Hotel name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address too long")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian contact number")
    private String contactNumber;

    @DecimalMin(value = "0.0", message = "Rating cannot be less than 0")
    @DecimalMax(value = "5.0", message = "Rating cannot be more than 5")
    private Double rating;

    @Size(max = 1000, message = "Description too long")
    private String description;
}