package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByHotelId(Long hotelId);
    boolean existsByBookingId(Long bookingId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.users.id = :userId OR r.booking.id IN (SELECT b.id FROM Booking b WHERE b.user.id = :userId)")
    void deleteByUserOrBookingUserId(@Param("userId") Long userId);
}
