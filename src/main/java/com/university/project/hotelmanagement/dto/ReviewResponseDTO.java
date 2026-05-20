package com.university.project.hotelmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private int rating;
    private String comment;
    private String username;
    private String userImageUrl;
    private String hotelName;
    private LocalDateTime createdAt;
}
