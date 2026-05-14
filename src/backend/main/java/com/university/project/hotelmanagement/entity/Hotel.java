package com.university.project.hotelmanagement.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@JsonPropertyOrder({ "id", "name", "state","district","address","contactNumber","rating","description","imageUrl" })
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String state;
    private String district;
    private String address;
    private String contactNumber;

    private Double rating;
    @Column(length = 1000)
    private String description;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] picture;
    private String pictureContentType;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    private List<Room> rooms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private UserEntity admin;
}
