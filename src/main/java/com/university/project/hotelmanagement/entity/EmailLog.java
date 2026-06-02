package com.university.project.hotelmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
@Data
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String toEmail;
    private String subject;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    private LocalDateTime sentAt;
}
