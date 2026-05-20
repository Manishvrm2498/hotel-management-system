package com.university.project.hotelmanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String imageUrl;
    private Long imageVersion;
    private boolean enabled;
    private LocalDateTime createdAt;
}
