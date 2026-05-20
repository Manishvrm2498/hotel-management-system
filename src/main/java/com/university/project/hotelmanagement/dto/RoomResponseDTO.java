package com.university.project.hotelmanagement.dto;

import com.university.project.hotelmanagement.enums.RoomType;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponseDTO {
    private Long id;
    private RoomType type;
    private double price;
    private int totalRooms;
    private String hotelName;
    private Long hotelId;
    private boolean isAvailable;
    private String imageUrl;
}
