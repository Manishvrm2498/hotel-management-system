package com.university.project.hotelmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String otp;
    private String otpToken;
    private String purpose;
    private LocalDateTime expiryTime;
    private int attempts;
    private boolean verified;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
