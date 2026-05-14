package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.booking.id IN (SELECT b.id FROM Booking b WHERE b.user.id = :userId)")
    void deleteByBookingUserId(@Param("userId") Long userId);
}
