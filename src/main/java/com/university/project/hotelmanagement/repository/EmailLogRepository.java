package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
}