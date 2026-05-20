package com.university.project.hotelmanagement.entity;

import com.university.project.hotelmanagement.enums.RoomType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "room")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoomType type;
    private double price;
    private int totalRooms;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] picture;
    private String pictureContentType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    private boolean available;
}
