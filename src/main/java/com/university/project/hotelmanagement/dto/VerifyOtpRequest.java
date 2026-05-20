package com.university.project.hotelmanagement.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String otp;
    private String otpToken;
}
